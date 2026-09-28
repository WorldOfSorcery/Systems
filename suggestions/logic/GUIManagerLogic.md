# GUIManager (logic additions)

**Package:** `me.hektortm.woSSystems.systems.guis`

## Logic Issues

### 1. `handleClickActions` mutates the cached `global_actions` list

Lines 181–182:
```java
List<String> actions = config.getGlobal_actions();
if (specific != null) actions.addAll(specific);
```
`config.getGlobal_actions()` returns the list directly from the cached `GUISlotConfig` object. Calling `addAll` on that reference permanently appends the left- or right-click actions to the cached global list. Every subsequent click on the same slot will see the accumulated extra actions. This is the same issue flagged in `GUIManager.md` — confirmed present in the source.

### 2. `openGUI` fires open-actions before the inventory is shown to the player

Lines 94–97:
```java
if (gui.getOpenActions() != null && !gui.getOpenActions().isEmpty()) {
    actionHandler.executeActions(player, gui.getOpenActions(), ...);
}
player.openInventory(inventory);
```
Open-actions (e.g. `close_gui`, `send_message`) run before `openInventory`. If an open-action calls `player.closeInventory()`, the subsequent `openInventory` still executes, opening the GUI immediately after the close action intended to cancel it. The action list should be executed after `openInventory`, or `close_gui` in open-actions should be handled specially.

### 3. `applyModel` method is defined but never called — dead code

Lines 286–295: `applyModel(ItemMeta, GUISlotConfig, Player)` reads `config.getModel()` and applies it as a tooltip style to the meta. However, `generateItemMeta` handles the model field itself (lines 212–220) and never calls `applyModel`. The method is dead code that will silently be ignored if someone tries to use it, and any future maintenance to the model handling logic would need to be applied to both `generateItemMeta` and `applyModel` or the behaviour would diverge.

### 4. Double null-check on `config.getTooltip()` is always true for the inner branch

Lines 218–224:
```java
if (config.getTooltip() != null && !config.getTooltip().isEmpty()) {
    if (Objects.equals(config.getTooltip(), "hidden")) { ... }
    else if (config.getTooltip() != null && !config.getTooltip().isEmpty()) { // ← redundant
```
The `else if` repeats the outer `if` condition exactly, making it always `true` when reached. This is harmless but is dead conditional logic that could confuse a future reader into thinking there is a case where the inner branch does not enter.
