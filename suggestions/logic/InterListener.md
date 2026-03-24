# InterListener

**Package:** `me.hektortm.woSSystems.systems.interactions`

## Logic Issues

### 1. `NPCClick` does not guard against a null interaction ID

Lines 80–83:
```java
int npcid = e.getNPC().getId();
String id = hub.getInteractionDAO().getNpcBound(npcid);
InteractionKey key = new InteractionKey("npc:"+npcid);
interactionManager.triggerInteraction(id, e.getClicker(), key);
```
`getNpcBound` may return `null` if the NPC has no bound interaction. `triggerInteraction` then calls `getInteraction(null)`, which queries the DAO with a null ID. Depending on how the DAO handles a null key this either throws `NullPointerException` or returns `null`, after which `triggerInteraction` sends the player the error message "§cThis is not configured correctly." — a confusing message for a completely unrelated NPC right-click. A null guard is required before calling `triggerInteraction`.

### 2. Block cooldown map grows without bound — memory leak

Line 26: `private final Map<Location, Long> blockCooldowns = new HashMap<>();`

Entries are added for every block that is interacted with (line 60) but are never removed. Over the lifetime of a server session, every unique `Location` that any player right-clicks or left-clicks accumulates an entry in this map. On a busy server with many interactable blocks, this grows indefinitely. A size-capped or time-expired data structure (e.g. a Guava `Cache` with expiry) should be used instead.

### 3. Redundant double lookup of `getBound` within a single event

Lines 63 and 70:
```java
if (hub.getInteractionDAO().getBound(blockLocation) != null) {
    ...
    interactionManager.triggerInteraction(hub.getInteractionDAO().getBound(blockLocation), p, key);
```
`getBound` is called twice for the same location within the same event handler. The result should be stored in a local variable from the first call and reused.
