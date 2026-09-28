# CitemListener (logic additions)

**Package:** `me.hektortm.woSSystems.systems.citems`

## Logic Issues

### 1. `triggerInteractionForDisplay` accesses item meta without a null check

Lines 113–114:
```java
PersistentDataContainer data = citem.getItemMeta().getPersistentDataContainer();
String interId = data.get(Keys.PLACED_ACTION.get(), PersistentDataType.STRING);
```
`citem.getItemMeta()` may return `null` for items whose material type does not support metadata. Calling `getPersistentDataContainer()` on a `null` meta throws `NullPointerException`. A null check on `getItemMeta()` is required before this line.

### 2. `handleCitemActions` called even when `handleCitemPlacement` already consumed the interaction

In `onPlayerInteract` lines 69–71:
```java
if (citemManager.isCitem(item)) {
    handleCitemPlacement(e, p, item, action);
    handleCitemActions(p, action, item);
}
```
Both methods are called unconditionally. When `handleCitemPlacement` succeeds and places a display (e.g. right-click on a block with a placeable citem), `handleCitemActions` still runs and triggers the `rightClickAction` interaction. A right-click that places an item should not also fire the right-click action. The `handleCitemActions` call should be skipped if placement occurred.

### 3. `onSwapHands` sets the item back in the main hand without checking the inventory slot

Line 302:
```java
e.getWhoClicked().getInventory().setItemInMainHand(item);
```
The event is `SWAP_OFFHAND` (F key), which means the item being acted on is in the main hand. `e.getCurrentItem()` returns the item in the *current* focused slot, but during a swap-offhand event, the slot may be in the off-hand or a container slot depending on context. Setting `setItemInMainHand` unconditionally always places the modified item into the main hand even if the event was triggered from a different slot, potentially duplicating or corrupting inventory state.
