# Quest Builder prototype nodes — spec for the plugin

**Status (September 2026):** built in the AdminPortal Quest Builder as a
**prototype**; **not implemented in the plugin**. The portal lets staff place,
configure, connect and save these nodes, marks them "Prototype", and shows a
banner on quests that use them. This document is what the plugin side is built
from. For the engine as it runs today, see [`quest-system.md`](quest-system.md).

Goal: quests use the rest of the game. They **start** on in-game events (talk to
an NPC, finish a dialog, enter a region …), **wait** for events mid-quest,
**branch** on dialog answers or on whichever event happens first, and **act**
on other systems (give items, unlock unlockables, open GUIs …).

Two example quests exist in the dev database (seeded by
`api/scripts/seed-quests.mjs`): `proto_herbalist_errand` (simple) and
`proto_mages_trial` (uses every prototype node). Walkthroughs at the end.

---

## 1. The six nodes

All data values the portal enters as text are **strings** in `params`, numbers
included (`"npc": "12"`, `"amount": "50"`); parse them on load. Node ids are
arbitrary strings; edges refer to them.

| Type | Kind | Inputs | Outputs (edge `sourceHandle`) |
|---|---|---|---|
| `trigger` | starts the quest | none | plain (`null`) → into `start`, or into a `condition` first |
| `wait_for` | waits | 1 | plain; **with a time limit:** `"done"` and `"timeout"` |
| `wait_any` ("First of") | waits, branches | 1 | `"event:0"`, `"event:1"`, … (one per event) and `"timeout"` if timed |
| `action` | does something | 1 | plain |
| `dialog_choice` | shows a dialog, branches | 1 | `"answer:<answer id>"` per answer, and `"closed"` |
| `stage` | quest-log chapter | 1 | plain |

`start` now also has an **input**: triggers (and requirement conditions) connect into it.

### 1.1 Event reference (used by `trigger`, `wait_for`, `wait_any`)

```json
{ "source": "npc", "event": "dialog_finished", "params": { "npc": "12", "npc_name": "Old Mage", "dialog": "mage_intro" } }
```

`source` + `event` pick an entry of the **event catalog** (§3), `params` holds
that entry's parameters. `npc_name` is only a label for the builder.

### 1.2 `trigger`

```json
{ "id": "t_mira", "type": "trigger", "data": {
    "label": "Talk to Mira",
    "source": "npc", "event": "right_click", "params": { "npc": "21", "npc_name": "Herbalist Mira" } } }
```

Starts the quest for the player the event happened to. Several triggers into
`start` = any of them starts it. The builder only allows a trigger to connect
into `start` or into a `condition`.

### 1.3 `wait_for`

```json
{ "id": "w_bass", "type": "wait_for", "data": {
    "label": "Catch 3 bass",
    "source": "fishing", "event": "caught", "params": { "fish": "bass" },
    "count": 3,
    "timeout_seconds": 600 } }
```

Waits until the event has happened `count` times (default 1) for this player.
`timeout_seconds` is optional (absent or ≤ 0 = no limit). With a limit, leave
through `"done"` or, when the limit runs out first, `"timeout"`; without one,
through plain edges.

### 1.4 `wait_any` ("First of")

```json
{ "id": "w_courage", "type": "wait_any", "data": {
    "label": "Courage: reach the summit",
    "events": [
      { "source": "region", "event": "entered", "params": { "region": "old_tower_summit" } },
      { "source": "npc", "event": "right_click", "params": { "npc": "41", "npc_name": "Tower Guardian" } } ],
    "timeout_seconds": 600 } }
```

2–4 events. The first one to happen wins: leave through `"event:<its index>"`,
and stop listening for the others. `"timeout"` when the limit runs out first.

### 1.5 `action`

```json
{ "id": "a_gold", "type": "action", "data": {
    "label": "Pay the player",
    "source": "currency", "action": "give", "params": { "currency": "gold", "amount": "50" } } }
```

`source` + `action` pick an entry of the **action catalog** (§4). Runs it, advances.

### 1.6 `dialog_choice`

```json
{ "id": "choice_offer", "type": "dialog_choice", "data": {
    "label": "Veyra's offer",
    "dialog_id": "veyra_offer",
    "answers": [ { "id": "accept", "label": "Accept" }, { "id": "bargain", "label": "Bargain" }, { "id": "refuse", "label": "Refuse" } ] } }
```

Shows dialog `dialog_id`; when the player picks an answer with id X, leave
through `"answer:X"`. Leaving the dialog without an answer → `"closed"`.
`answers[].id` are the dialog's answer ids (the portal can load them from the
dialog); `label` is only shown in the builder. An answer picked that has no
edge: treat like `"closed"`. Edges may loop back into the same node (the
Mage's Trial "Bargain" branch does: message → back to the choice).

### 1.7 `stage`

```json
{ "id": "stage_trials", "type": "stage", "data": {
    "title": "Chapter 2: Three Trials",
    "text": "Prove your knowledge, your patience and your courage.",
    "checkpoint": true } }
```

Sets the quest's current stage (title + text) in the player's quest log, advances.
`checkpoint`: the quest can resume here (see §5.5).

### 1.8 Requirements before the quest exists

```
trigger ──► condition ──yes──► start
                     └──no───► message   ("Help Mira first, then come back.")
```

A `condition` between a trigger and `start` is a **requirement**. It's evaluated
when the trigger fires, before the quest starts. The `"no"` side may lead to
nodes that run once and end there (message, command, action) — no quest state
exists yet. Waiting nodes are not allowed on that side (the builder doesn't
stop it yet; refuse on load, see §5.6).

---

## 2. Edge handles, summarised

| From | `sourceHandle` values |
|---|---|
| `trigger`, `action`, `stage`, `wait_for` without a limit | `null` |
| `wait_for` with a limit | `"done"`, `"timeout"` |
| `wait_any` | `"event:0"` … `"event:3"`, `"timeout"` |
| `dialog_choice` | `"answer:<id>"`, `"closed"` |
| existing: `condition` / `check_variable` / `parallel` | `"yes"`/`"no"` · `"true"`/`"false"`/`"else"` · `"branch_N"` |

Edges into a `merge` carry `targetHandle: "branch_N"` (which input). The engine
counts arrivals and can keep ignoring it.

---

## 3. Event catalog

Defined in the portal: `AdminPortal/src/modules/quest_builder/quest-events.ts`.
"Hook point" is where the plugin can report the event today; ✱ = no hook exists yet.

| `source` | params (common) | `event` | extra params | Hook point in WoSSystems |
|---|---|---|---|---|
| `npc` | `npc` (Citizens id), `npc_name` (label only) | `right_click` | — | `InterListener.NPCClick` (`NPCRightClickEvent`) |
| | | `dialog_finished` | `dialog` | ✱ needs "dialog finished" (see quest-system.md issue 1) + which NPC started it (§5.4) |
| | | `answer_picked` | `dialog`, `answer` | answer callbacks in `DialogDAO.buildDialog` + which NPC (§5.4) |
| `dialog` | `dialog` | `finished` | — | ✱ dialog finished signal |
| | | `answer_picked` | `answer` | `DialogDAO.buildDialog` → `Answer.Builder.addCallback` |
| `interaction` | `interaction` | `triggered` | — | `InteractionManager.triggerInteraction` |
| `region` | `region` (WorldGuard id) | `entered`, `left` | — | `QuestListener.onPlayerMove` already computes the regions; diff against the previous set per player |
| `citem` | `citem` | `obtained` | — | `CitemManager.giveCitem` + item pickup |
| | | `used` | — | `CitemListener.onPlayerInteract` |
| `gui` | `gui` | `slot_clicked` | `page`, `slot` | `GUIManager.onInventoryClick` |
| `stat` | `stat` | `reached` | `value` | `StatsManager.modifyStat` (fire when the value crosses `value` upwards) |
| `currency` | `currency` | `balance_reached` | `amount` | `EcoManager.modifyCurrency` (balance after ≥ `amount`) |
| | | `earned` | `amount` | `EcoManager.modifyCurrency` with GIVE (count the amount towards `amount`) |
| `cooldown` | `cooldown` | `expired` | — | `CooldownManager.run` → `CooldownDAO.expireDue` returns `Expired(player, cooldownId)` |
| `loottable` | `loottable` | `opened` | — | `LoottableManager.triggerLoottable` |
| `unlockable` | `unlockable` | `unlocked` | — | `UnlockableManager.modifyUnlockable` (GIVE) |
| `fishing` | `fish` | `caught` | — | `FishingListener.onFish` (after the catch is decided) |
| `timeevent` | `timeevent` | `started` | — | `TimeEvents.checkForActivity` (fires for every online player) |
| `quest` | `quest` | `completed`, `failed` | — | `QuestCompleteEvent` |

## 4. Action catalog

Defined in the portal: `AdminPortal/src/modules/quest_builder/quest-actions.ts`.

| `source` | params (common) | `action` | extra params | In WoSSystems |
|---|---|---|---|---|
| `citem` | `citem` | `give` | `amount` (optional, 1) | `CitemManager.giveCitem` |
| | | `take` | `amount` (optional, 1) | ✱ no take method (only `hasCitemAmount`) — add one |
| `currency` | `currency` | `give`, `take` | `amount` | `EcoManager.modifyCurrency(…, GIVE / TAKE, "quest", questId)` |
| `stat` | `stat` | `add` | `amount` | `StatsManager.modifyStat(…, GIVE)` |
| | | `set` | `value` | `StatsManager.modifyStat(…, SET)` |
| `cooldown` | `cooldown` | `start` | — | `CooldownDAO.giveCooldown` (+ the cooldown's start interaction, as the `cooldown` action does for local ones) |
| | | `reset` | — | `CooldownDAO.removeCooldown` |
| `unlockable` | `unlockable` | `unlock`, `lock` | — | `UnlockableManager.modifyUnlockable(…, GIVE / TAKE)` |
| `gui` | `gui` | `open` | — | `GUIManager.openGUI` |
| `interaction` | `interaction` | `run` | — | `InteractionManager.triggerInteraction(id, player, null)` |
| `loottable` | `loottable` | `roll` | — | `LoottableManager.triggerLoottable` |
| `quest` | `quest` | `start` | — | `QuestManager.startQuest` (chains quests) |
| `waypoint` | — | `show` | `label`, `world`, `x`, `y`, `z` | ✱ new: e.g. a boss bar / compass pointing at the spot (`BossBarManager` exists) |
| | | `clear` | — | ✱ new |

---

## 5. Engine design (proposal)

### 5.1 One event bus

```java
questEvents.fire(player, "npc", "right_click", Map.of("npc", "21"));
```

Every hook point in §3 calls this with the facts it knows. The bus matches the
event against:

- **trigger nodes** — an index built when quests load (and rebuilt for one quest
  on reload): `(source, event) → [(questId, triggerNodeId)]`;
- **waiting nodes** of the player's active quests (`wait_for`, `wait_any`).

A node's `params` **match** the facts when every non-empty param equals the fact
of the same key (case-insensitive, trimmed), except the "reaches"-style numbers:

| Event | Param | Matches when |
|---|---|---|
| `stat/reached` | `value` | fact `value` ≥ param |
| `currency/balance_reached` | `amount` | fact `balance` ≥ param |
| `currency/earned` | `amount` | add fact `amount` to the node's progress; done at ≥ param |

Labels (`npc_name`) are never matched.

### 5.2 Triggers

On a matching trigger:

1. Skip if the player already has the quest active, or it's done and not
   repeatable (§6.1).
2. Walk the trigger's out-edge. `start` → `startQuest`. `condition` → evaluate
   (same `ConditionHandler`) and follow `yes` / `no`; `no`-side nodes run once
   without quest state.
3. Log which trigger started it (useful in `/quest status`).

### 5.3 `wait_for`, `wait_any`

- On enter: register the node as waiting (it's in `activeNodes`); progress in
  `objectiveProgress` (reuse it: node id → count).
- Time limits must **survive restarts and relogs**: store the deadline (epoch
  millis) in the state — new field `deadlines: Map<nodeId, Long>` — and check it
  on a repeating task and on join. (Today's `delay` has this bug; fix it the same way.)
- `wait_any`: on the first match leave through `event:<index>` and remove the
  node from `activeNodes` (so the other events no longer match).

### 5.4 Dialogs and NPCs

- `dialog_choice`: build the dialog with an extra callback per answer that calls
  `questManager.onDialogAnswer(player, questId, nodeId, answerId)`; the "closed"
  path needs LuxDialogues to report a dialog ending without an answer.
- NPC-scoped dialog events (`npc/dialog_finished`, `npc/answer_picked`) need to
  know which NPC a dialog belongs to. Proposal: when an NPC click starts a
  dialog (through its interaction), remember `player → npc id` for that dialog
  and add `npc` to the facts of the dialog's events.

### 5.5 Stages and checkpoints

- New state fields: `stage` (`{nodeId, title, text}`), `checkpoint` (node id).
- `/quest status` (and later a quest log GUI / scoreboard) shows the current
  stage's title and text.
- Proposal: a failed quest (or `/quest restart`) resumes at the last
  checkpoint's node with that node's progress reset, instead of from `start`.

### 5.6 Loading and validation

When a quest loads, refuse it (log for staff, don't start it) if:

- it has a node type the plugin doesn't know (instead of today's skip);
- a trigger doesn't lead to `start` (directly or through conditions);
- a waiting node sits on a requirement's `no` side;
- an edge leaves through a handle the node doesn't have.

The portal should show the same checks in the builder later.

---

## 6. Open questions

1. **Repeating.** Can a quest run again after it's completed? Proposal:
   quest-level `repeat: "never" | "after_fail" | "always"` plus an optional
   cooldown (the existing cooldown system). Not in the portal yet.
2. **Several triggers at once.** Two quests triggered by the same click — start
   both, or ask? Proposal: start every eligible one, in quest-id order.
3. **Waiting on offline players.** Do time limits run while offline? Proposal:
   yes (deadline = wall clock), checked on join.
4. **NPC names.** The portal only knows NPC ids. A later step could sync Citizens
   NPCs (id, name) to wos-api so the builder can suggest them like other content.
5. **Quest log UI.** Stages need somewhere to show: `/quest status` first, a GUI
   or scoreboard later.

---

## 7. Suggested build order

1. Fix the known issues in `quest-system.md` (dialog finished, unknown node
   types refused, persistent deadlines for `delay`).
2. Event bus + `trigger` for `npc/right_click` and `quest/completed`; `start`
   with requirements.
3. `wait_for` (+ time limits) and `wait_any` for the same events.
4. `action` (all entries except waypoint), `stage` + `/quest status`.
5. `dialog_choice` and the dialog events (needs the LuxDialogues end signal).
6. The remaining event sources, one system at a time.
7. Waypoints, checkpoint resume, repeat rules.

---

## 8. Example quests (dev database)

### The Herbalist's Errand — `proto_herbalist_errand`

```
[Trigger] NPC #21 "Herbalist Mira" right-clicked
   ▼
[Start] → [Stage] Catch three bass
   → [Wait for] fishing: bass caught × 3
   → [Stage] Bring the fish back
   → [Wait for] NPC #21 right-clicked
   → [Message] "Mira: Just what I needed…"
   → [Action] currency gold: give 50
   → [Action] unlockable herbalist_friend: unlock
   → [End] success
```

### The Mage's Trial — `proto_mages_trial`

```
[Trigger] NPC #40 "Archmage Veyra" right-clicked
   ▼
[Condition] has_unlockable herbalist_friend ──no──► [Message] "Help Mira first"
   │ yes
[Start] → [Stage ✓] Chapter 1: The Offer
   → [Dialog choice] veyra_offer
        ├─ accept  ─► [Action] waypoint: The Old Tower
        ├─ bargain ─► [Message] "500 gold, and my respect" ─► back to the Dialog choice
        ├─ refuse  ─► [End] failed
        └─ closed  ─► [End] failed
   [Action] waypoint → [Stage ✓] Chapter 2: Three Trials → [Parallel ×3]
        ├─ Knowledge: [Wait for] item book_of_knowledge obtained ───────────┐
        ├─ Patience:  [Wait for] fishing: catfish caught × 2 ───────────────┤
        └─ Courage:   [First of] A: region old_tower_summit entered          │
                                 B: NPC #41 "Tower Guardian" right-clicked   │
                                 ⏱ 600 s ─► [Message] "wards reset" ─► back │
                        A / B ──────────────────────────────────────────────┤
   [Merge ×3] ◄───────────────────────────────────────────────────────────────┘
   → [Stage ✓] Chapter 3: The Final Test → [Action] waypoint: clear
   → [Wait for] NPC #40 finishes dialog veyra_final (⏱ 900 s ─► [End] failed)
   → [Reward] 500 gold → [Action] unlock mage_apprentice → [Action] stat quest_test +1
   → [Message] title "Apprentice of the Archmage" → [End] success
```

Content these reference that doesn't exist yet: NPCs #21, #40, #41; dialogs
`veyra_offer`, `veyra_final`; unlockables `herbalist_friend`, `mage_apprentice`;
region `old_tower_summit`. (`gold`, `bass`, `catfish`, `book_of_knowledge` exist.)

Note on the Parallel above: with today's engine a parallel releases when each
branch's *first* node completes (quest-system.md issue 4); the `merge` is what
waits for all three trials.
