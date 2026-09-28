# InteractionManager (logic additions)

**Package:** `me.hektortm.woSSystems.systems.interactions`

## Logic Issues

### 1. Dead `conditionList.isEmpty()` check inside the `"one"` branch

Lines 147–148:
```java
if ("one".equalsIgnoreCase(action.getMatchType())) {
    shouldRun = conditionList.isEmpty() || conditionList.stream().anyMatch(...);
```
This code is only reached when `!conditionList.isEmpty()` has already been confirmed (the outer `if` on line 145 guards entry). Therefore `conditionList.isEmpty()` inside the branch is always `false` and the `||` short-circuits incorrectly — it makes the `"one"` branch always pass when conditions exist. The `conditionList.isEmpty()` sub-expression should be removed, leaving only:
```java
shouldRun = conditionList.stream().anyMatch(cond -> conditions.evaluate(player, cond, key));
```
This matches the existing flag in the `InteractionManager.md` suggestion but is confirmed by direct reading of the source.

### 2. `executeActions` dispatched onto main thread inside a loop that may already be on main thread

Line 156:
```java
Bukkit.getScheduler().runTask(WoSSystems.getPlugin(WoSSystems.class), () -> {
    actionHandler.executeActions(player, action.getActions(), ...);
});
```
`triggerInteraction` is called from block/NPC click events (always on the main thread), from `CooldownManager.onCooldownExpire` (explicitly switched to main thread), and from `ActionHandler`'s cooldown action (also on main thread). Scheduling a redundant `runTask` from within an already-synchronous context adds one tick of latency to every interaction that fires from a player click, delaying feedback perceptibly. Only paths that are genuinely async (none confirmed in current code) need the `runTask` wrapper.

### 3. Interaction task loads cache data async then switches back to main thread — data may be stale

Lines 81–84 in `interactionTask()`:
```java
Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
    List<Interaction> interactions = hub.getInteractionDAO().cache();
    Bukkit.getScheduler().runTask(plugin, () -> {
        for (Interaction inter : interactions) { ... }
```
The interaction list is captured in the async phase, then iterated on the main thread one tick later. If an interaction is created or deleted between the async read and the main-thread iteration, the loop processes a stale snapshot. More critically, `hub.getInteractionDAO().cache()` returns the live cache collection. If any other thread mutates it (e.g. a webhook invalidation running asynchronously) while the main thread is iterating, a `ConcurrentModificationException` is possible. The cache method should return a defensive copy, or the task should work entirely on the main thread.
