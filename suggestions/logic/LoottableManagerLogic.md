# LoottableManager (logic additions)

**Package:** `me.hektortm.woSSystems.systems.loottables`

## Logic Issues

### 1. `lt` is dereferenced without a null check — NPE on unknown loottable ID

Lines 29–31:
```java
Loottable lt = hub.getLoottablesDAO().getLoottable(id);
LoottableItem item = lt.getRandom();   // NPE if lt is null
```
If `id` does not match any configured loottable, `getLoottable` returns `null` and `lt.getRandom()` throws `NullPointerException`. This is the same issue flagged in `LoottableManager.md` — confirmed by source reading.

### 2. `OfflinePlayer` parameter is unconditionally cast to `Player` for every branch

Lines 37–41:
```java
case DIALOG    -> hub.getDialogDAO().getDialog(item.getValue(), source, (Player) player);
case CITEM     -> plugin.getCitemManager().giveCitem(source, (Player) player, ...);
case GUI       -> plugin.getGuiManager().openGUI((Player) player, item.getValue());
case INTERACTION -> plugin.getInteractionManager().triggerInteraction(item.getValue(), (Player) player, null);
case COMMAND   -> actionHandler.executeActions((Player) player, ...);
```
All five branches cast `player` (typed as `OfflinePlayer`) to `Player`. If an offline player is passed, every branch throws `ClassCastException`. This is flagged in `LoottableManager.md` but confirmed here: no branch has a null or online-player guard.
