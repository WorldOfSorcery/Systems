# ProfileListener

**Package:** `me.hektortm.woSSystems.systems.profiles`

## Logic Issues

### 1. `onProfileClick` casts `getWhoClicked()` to `Player` without checking

Line 80:
```java
Player p = (Player) e.getWhoClicked();
```
`InventoryClickEvent.getWhoClicked()` returns a `HumanEntity`, which can be a `Player` in normal gameplay but is declared as the base type. In an edge case (e.g. a fake entity or a test harness), this cast throws `ClassCastException`. An `instanceof` guard should be used.

### 2. `onProfileClick` accesses `item.getItemMeta()` without a null check

Lines 83–86:
```java
ItemStack item = e.getCurrentItem();
ItemMeta meta = item.getItemMeta();
PersistentDataContainer data = meta.getPersistentDataContainer();
String cmd = data.get(manager.getCmdKey(), PersistentDataType.STRING);
```
`e.getCurrentItem()` returns `null` for an empty slot, and `item.getItemMeta()` can also return `null` for certain materials. Neither is checked: `item.getItemMeta()` throws `NullPointerException` if `item` is `null`, and `meta.getPersistentDataContainer()` throws `NullPointerException` if `meta` is `null`. Null guards for both `item` and `meta` are required.

### 3. `Bukkit.dispatchCommand(p, cmd)` runs a command from an inventory click — async-unsafe and command can be null

Line 87:
```java
Bukkit.dispatchCommand(p, cmd);
```
If `cmd` is `null` (the PDC key is absent from the item), `dispatchCommand` throws `NullPointerException`. Additionally, the `viewProfile` inventory is a `static` field shared across all players; if two players open someone's profile simultaneously, one `setItem` call can overwrite the item another player just clicked before this handler reads it. The `cmd` key may therefore resolve to the wrong player's command.

### 4. `onProfileClose` always calls `updateProfile` even when the profile inventory was closed programmatically

`onProfileClose` fires for every `InventoryCloseEvent`, including when the server closes inventories on quit or when another event calls `p.closeInventory()`. There is no state flag preventing a redundant or spurious DB write when the edit-profile inventory closes for reasons other than explicit player action.

### 5. `backgroundUni` is always hardcoded to `"e"` in `updateProfile` call

Line 73:
```java
manager.updateProfile(p, pictureUni, pictureID, backgroundUni, "e");
```
The `backgroundID` parameter (last argument to `updateProfile`) is the string literal `"e"` regardless of what is actually in slot 7. The `backgroundItem` at slot 7 is read and its PDC key is extracted into `backgroundUni`, but the corresponding background *ID* key is never read from the PDC. Instead the constant `"e"` is passed, meaning the background ID is always overwritten with `"e"` in the database whenever the profile editor is closed.
