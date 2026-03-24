# BackpackListener

**Package:** `me.hektortm.woSSystems.systems.backpack`

## Logic Issues

### 1. Shulker box contents are not saved when the server closes the inventory on disconnect

`onInventoryClose` saves the inventory contents back into the shulker box item and clears the metadata. This only fires on a normal `InventoryCloseEvent`. If a player disconnects while their shulker box is open, Bukkit closes all open inventories on the *quit* event — but `onInventoryClose` may or may not fire (behaviour is server-implementation-specific). In practice on Paper, `InventoryCloseEvent` does fire on quit; however, if the server crashes or is killed (`SIGKILL`), the event never fires and the shulker box contents written to the item will not reflect what was in the inventory at disconnect time. This is a data-loss risk for any server that does not do clean shutdowns.

### 2. `onRightClickShulker` does not check the action type — fires on left-click

`PlayerInteractEvent` fires for right-clicks, left-clicks, and other actions. The handler does not filter on `event.getAction()`, so it will attempt to open the shulker box inventory on `LEFT_CLICK_AIR`, `LEFT_CLICK_BLOCK`, and `PHYSICAL` actions as well as right-clicks. The shulker box should only open on `RIGHT_CLICK_AIR` or `RIGHT_CLICK_BLOCK`.

### 3. `getMetadata` call without checking list size — `IndexOutOfBoundsException` possible

Line 45:
```java
ItemStack item = (ItemStack) event.getPlayer().getMetadata("shulker_box").get(0).value();
```
If `hasMetadata("shulker_box")` returns `true` but the metadata list is somehow empty (e.g. from a different plugin that registered metadata with the same key but no value), `.get(0)` throws `IndexOutOfBoundsException`. The check on line 43 (`hasMetadata`) only guarantees the list is non-empty in Bukkit's implementation, but a defensive size check is more robust.
