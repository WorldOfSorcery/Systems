# FishingListener (logic additions)

**Package:** `me.hektortm.woSSystems.systems.professions.fishing`

## Logic Issues

### 1. `triggerInteraction` called with a potentially null interaction ID

Lines 46–47:
```java
if (citem != null) {
    caughtItemEntity.setItemStack(citem);
    interactionManager.triggerInteraction(fishingItem.getInteraction(), player, null);
}
```
`fishingItem.getInteraction()` may return `null` if the fishing item has no interaction configured. `triggerInteraction(null, ...)` then calls `getInteraction(null)` on the DAO, which queries with a null key. Depending on DAO implementation this either returns `null` or throws. If `null` is returned, `triggerInteraction` sends the player "§cThis is not configured correctly." — a confusing error for a successful catch. A null guard is required before calling `triggerInteraction`.

### 2. `event.getCaught()` cast to `Item` without type check

Line 39:
```java
Item caughtItemEntity = (Item) event.getCaught();
```
`PlayerFishEvent.getCaught()` is documented to return `null` if the state is not `CAUGHT_FISH` (or `CAUGHT_ENTITY`). The outer guard `if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH)` on line 30 should guarantee a non-null `Item` entity, but the Bukkit API does not formally guarantee the runtime type is `Item` — it is `Entity`. If the server wraps the caught entity differently, the cast throws `ClassCastException`. An `instanceof` check before the cast is safer.
