# RegionHandler (logic additions)

**Package:** `me.hektortm.woSSystems.systems.regions`

## Logic Issues

### 1. Enter-interaction fires for every player already in a region, not just when entering

Lines 57–59:
```java
if (enterInteraction != null && !plugin.getPlayerRegions().containsValue(newRegionId)) {
    plugin.getInteractionManager().triggerInteraction(enterInteraction, player, null);
}
```
The guard checks `!plugin.getPlayerRegions().containsValue(newRegionId)` — i.e., it fires the enter-interaction only if **no player in the entire server** is currently tracked in that region. As soon as any one player enters a region, every subsequent player who enters the same region will *not* trigger the enter-interaction (because `containsValue` now returns `true`). The correct check is whether *this specific player* was previously outside the region, which requires comparing `currentRegionId` (for the player) with `newRegionId`.

### 2. Leave-interaction is never triggered

`LEAVE_INTERACTION` flag is read (line 51: `String leaveInteraction = region.getFlag(WoSSystems.LEAVE_INTERACTION)`) but `leaveInteraction` is never used. The variable is set but no code calls `triggerInteraction` with it. Region exit events are therefore silently ignored.

### 3. `updateRegion` is called on every movement packet — world state modified on main thread per packet

`onPlayerMove` fires for every position change, including sub-block movements. `updateRegion` performs a WorldGuard region query and calls `Bukkit.dispatchCommand` indirectly via `triggerInteraction` on every packet. This was flagged in `RegionHandler.md` but the source confirms the `onPlayerMove` guard fires for *all* movement including identical block positions, meaning the block-boundary throttle described in the existing suggestion is not implemented.
