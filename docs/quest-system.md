# Quest System — Code Reference

How the quest engine in WoSSystems works **today** (September 2026). The portal's
newer prototype nodes (Trigger, Wait for, First of, Action, Dialog choice, Stage)
are **not** implemented here yet; their spec is
[`quest-builder-prototype.md`](quest-builder-prototype.md).

## Overview

Quests are built in the AdminPortal's Quest Builder as **ReactFlow flowcharts**
(nodes + edges) and stored by **wos-api** in `content.quests` (JSON `nodes` and
`edges`). The plugin loads them through the API and runs each one as a flow
engine per player. Player progress is written back through the API.

---

## Package Structure

```
systems/quests/
├── model/
│   ├── Quest.java              # Quest definition (nodes + edges)
│   ├── QuestNode.java          # A single flowchart node
│   ├── QuestEdge.java          # A connection between two nodes
│   └── PlayerQuestState.java   # Mutable per-player session
├── cmd/
│   ├── QuestCommand.java       # /quest root command
│   └── sub/
│       ├── Start.java          # /quest start <id> [player]
│       ├── Status.java         # /quest status [player]
│       └── Help.java           # /quest help
├── QuestDAO.java               # Definitions + player progress (via wos-api)
├── QuestManager.java           # Flow engine + public API
├── QuestListener.java          # Bukkit events → objective progress
└── QuestCompleteEvent.java     # Custom Bukkit event fired on completion

utils/ActionHandler.java        # the quest_progress action
```

---

## Data Flow

```
AdminPortal Quest Builder
        │  PATCH /v1/content/quests/{id}   (nodes + edges JSON)
        ▼
wos-api  content.quests
        │  GET /v1/content/quests  (ContentStore "quests", preloaded at startup;
        │                          one quest reloaded when the portal signals an
        │                          edit via the webhook /api/invalidate)
        ▼
Quest cache (id → Quest)
        │  /quest start <id>   (the only way a quest starts today)
        ▼
QuestManager.startQuest()  →  PlayerQuestState (in RAM, per player per quest)
        │                        │
        │                        └─ PUT /v1/players/{uuid}/quests/{id} after every change
        ▼
Flow engine (enterNode / advanceFromNode)
        ├── Bukkit events → QuestListener → progressObjective()
        └── on end → QuestCompleteEvent
                     + POST /v1/players/{uuid}/quests/{id}/completions
```

---

## Key Classes

### `QuestNode.java`

Wraps one node from the ReactFlow JSON. `data` is the raw `JsonObject` with all
node-specific fields.

| Method | What it returns |
|---|---|
| `getId()` | Node ID (e.g. `"node_1"`) |
| `getType()` | Node type string (see node types below) |
| `getData()` | Raw JSON data object |
| `getLabel()` | Display label |
| `getDialogId()` | For `dialog` nodes: which dialog to play |
| `getObjectiveType()` | For `objective` nodes: `kill`, `collect`, `talk`, `reach`, … |
| `getObjectiveTargetId()` | For `objective` nodes: what to match (e.g. `"zombie"`) |
| `getObjectiveQuantity()` | How many are needed (default `1`) |
| `getObjectiveLabel()` | Text shown to the player |
| `getRewards()` | For `reward` nodes: JsonArray of rewards |
| `isSuccess()` | For `end` nodes: true = completed, false = failed |
| `getEndMessage()` | For `end` nodes: message shown to the player |
| `getBranchCount()` | For `merge` nodes: how many arrivals to wait for |

### `QuestEdge.java`

A directed connection: `getSource()` / `getTarget()` (node ids) and
`getSourceHandle()` — `null` for a plain edge, otherwise the output it leaves
from (`"yes"`, `"no"`, `"true"`, `"false"`, `"else"`, `"branch_N"`).
`isBranch()` is `sourceHandle != null`. The portal also saves `targetHandle`
(e.g. which input of a `merge` an edge enters), but the plugin ignores it.

### `Quest.java`

- `getStartNode()` — the **first** node of type `start` (a quest should have one)
- `getNode(id)` — any node by id
- `getNextNodeIds(nodeId, handle)` — targets of the edges leaving `nodeId` whose
  `sourceHandle` equals `handle` (`null` → only edges without a handle)
- `getBranchEdges(nodeId)` — every edge leaving `nodeId` that has a handle

### `PlayerQuestState.java`

One player's session in one quest.

| Field | Type | Purpose |
|---|---|---|
| `status` | `String` | `"active"`, `"completed"` or `"failed"` |
| `activeNodes` | `Set<String>` | Nodes the player is currently at |
| `completedNodes` | `Set<String>` | Every node passed through |
| `objectiveProgress` | `Map<String, Integer>` | Count per objective node |
| `variables` | `Map<String, Double>` | Quest variables set by `variable` nodes |
| `mergeProgress` | `Map<String, Integer>` | Arrivals counted per merge node |

Persisted as JSON (`quest_id`, `status`, `active_nodes`, `completed_nodes`,
`objective_progress`, `variables`, `merge_progress`).

### `QuestDAO.java`

- **Definitions:** a `ContentStore<Quest>` over `GET /v1/content/quests`
  (preloaded at startup, reloaded per quest on a portal edit).
- **`loadPlayerStates(uuid)`** — the player's `active` quests, from their
  `PlayerSession` (loaded from the API on join).
- **`savePlayerState(state)`** — updates the session and
  `PUT /v1/players/{uuid}/quests/{questId}`.
- **`recordCompletion(uuid, questId, success)`** —
  `POST /v1/players/{uuid}/quests/{questId}/completions`; on success also adds
  the quest to the session's completed set.
- **`hasCompleted(uuid, questId)`** — ever completed successfully (from the session).

### `QuestManager.java`

The flow engine.

```java
questManager.startQuest(player, "quest_id");                 // start (false if unknown or already active)
questManager.progressObjective(player, questId, nodeId, 1);  // advance an objective
questManager.onDialogComplete(player, dialogId);             // advance dialog nodes waiting on it
questManager.getActiveState(uuid, questId);
questManager.getActiveStates(uuid);
questManager.hasCompleted(uuid, questId);
```

Private engine methods:

- `enterNode()` — runs a node by type (below)
- `advanceFromNode(…, handle)` — marks the node done, enters the targets of the
  edges leaving it through `handle`, then checks parallels, and **completes the
  quest (as a success) when no node is active any more**
- `checkParallelCompletion()` — releases `parallel` nodes whose branches are done
- `completeQuest()` — status, end message, `QuestCompleteEvent`, persists, drops the session

---

## Node Types and How They Work

Unless noted, a node is entered, does its thing, and advances at once along its
plain (no-handle) edges.

| Type | Data | Behaviour |
|---|---|---|
| `start` | `label` | Advances immediately. |
| `dialog` | `dialog_id` | Builds the dialog for the player (`DialogDAO.buildDialog`) and **waits** for `onDialogComplete(player, dialog_id)`. Empty `dialog_id` → advances immediately. See Known issues. |
| `interaction` | `interaction_id` | Triggers the interaction, advances. |
| `message` | `message_type` (`chat`, `title`, `subtitle`, `actionbar`), `text` (`&` colours), `fade_in` / `stay` / `fade_out` ticks | Sends it, advances. |
| `teleport` | `world`, `x`, `y`, `z`, `yaw`, `pitch` | Teleports (main thread), advances. |
| `variable` | `key`, `operation` (`set`/`add`/`subtract`/`multiply`), `value` | Changes the quest variable, advances. |
| `condition` | `condition_key`, `value`, `parameter` | `ConditionHandler.evaluate` → follows `"yes"` or `"no"`. |
| `check_variable` | `key`, `operator` (`==` `!=` `>` `>=` `<` `<=`), `compare_value` | Follows `"true"`, `"false"`, or `"else"` (variable not set / not a number). |
| `parallel` | `branch_count`, `branch_labels` | Enters **every** handled edge target (`branch_0`, `branch_1`, …); stays active until released (see Known issues for what "done" means). Then follows its plain edges. |
| `merge` | `branch_count`, `branch_labels` | Counts arrivals; advances once the count reaches `branch_count`. |
| `command` | `executor` (`console`/`player`), `commands[]` (`%player%`) | Runs them (main thread), advances. |
| `delay` | `ticks` (default 20) | Advances after the delay (Bukkit scheduler). |
| `reward` | `rewards[]` of `{type, id, amount}` | `currency` → `EcoManager.modifyCurrency(…, GIVE, "quest", questId)`; `item` → `CitemManager.giveCitem`; `loottable` → `LoottableManager.triggerLoottable`; `command` → console command. Sends "Rewards granted!", advances. |
| `objective` | `objective: {type, target_id, quantity, label}` | Registers progress 0 and **waits** for `progressObjective` to reach `quantity`. |
| `end` | `success`, `message` | Completes the quest (completed / failed). |
| anything else | — | Logs a warning and **advances** (see Known issues). |

### Objective types — what triggers them

All in `QuestListener`. `checkObjectives(player, type, typeName, displayName)`
progresses every active objective of that type whose `target_id` matches:
empty `target_id` matches anything; otherwise case-insensitive against the
type name, or the display name (raw, or with spaces as underscores).

| Objective | Event | Matched against |
|---|---|---|
| `kill` | `EntityDeathEvent` (player is the killer) | entity type (`zombie`) or custom name |
| `collect` | `EntityPickupItemEvent` | material (`diamond`) or item display name |
| `craft` | `CraftItemEvent` | result material or display name |
| `interact` | `PlayerInteractEntityEvent` (right-click a living entity) | entity type or custom name |
| `talk` | LuxDialogues `DialogueStartEvent` | the dialog id (fires when a dialog **starts**) |
| `reach` | `PlayerMoveEvent` (block changes only) + WorldGuard | region ids at the player (including parent regions) or the world name |
| `custom` | none — external | call `progressObjective`, e.g. the `quest_progress` action |

The `quest_progress` action (usable in interactions, dialogs, GUIs …):

```
quest_progress <quest_id> <node_id> [amount]
```

### Edge routing

| `sourceHandle` | From | Meaning |
|---|---|---|
| `null` | any node | plain forward edge |
| `"yes"` / `"no"` | `condition` | condition true / false |
| `"true"` / `"false"` / `"else"` | `check_variable` | matched / not matched / variable missing |
| `"branch_0"`, `"branch_1"`, … | `parallel` | one branch each |

---

## Known issues (found September 2026)

These are in today's code; fix them before (or while) building the prototype nodes.

1. **Dialog nodes never finish.** `onDialogComplete` is never called: nothing
   listens for a dialog ending. A quest that reaches a `dialog` node waits there
   forever. Needs a "dialog finished" signal from LuxDialogues (an end event, or
   a callback on the last page) that calls `onDialogComplete`.
2. **Dialog nodes may not show the dialog.** `enterNode` calls
   `DialogDAO.buildDialog(…)` and discards the returned `Dialogue`. Other callers
   do the same, so building probably also shows it — confirm against the
   LuxDialogues API.
3. **Unknown node types are skipped, not refused.** The `default` branch logs a
   warning and advances. Quests using the portal's prototype nodes would run in
   game with those steps silently skipped. Until they're implemented, refuse to
   start such quests (or treat unknown types as "wait forever" and tell staff).
4. **Parallel releases on each branch's first node.** `checkParallelCompletion`
   checks only that every branch's *target* (the first node of each branch) is
   completed, not the whole branch. Use a `merge` to wait for whole branches.
5. **Delays don't survive a restart or relog.** The scheduled advance lives only
   in memory; a player who logs out (or a server that restarts) during a `delay`
   stays on that node forever.
6. **Completed quests can be started again.** `startQuest` only refuses a quest
   that is *active*; `/quest start` on a completed one starts it over. There is
   no repeat rule yet.
7. **A quest with no `end` node completes as a success** when its last node
   finishes (no active nodes left).
8. **`talk` objectives fire on dialog start**, not on finishing it.

---

## Permissions

| Permission | Who needs it |
|---|---|
| `quest.start` | A player starting a quest for themselves |
| `quest.admin.start` | Staff starting a quest for another player |

---

## Extending — adding an objective type

1. Add an `@EventHandler` in `QuestListener`.
2. Take a `typeName` and `displayName` from the event.
3. Call `checkObjectives(player, "your_type", typeName, displayName)`.
4. Add `your_type` to the portal's objective types (`ObjectiveType` in the
   AdminPortal's `quest.ts`) so it can be picked.

No changes to `QuestManager` or `QuestNode` are needed.
