# Chest (loottable sub-command)

**Package:** `me.hektortm.woSSystems.systems.loottables.cmd.sub`

## Logic Issues

### 1. `lt` dereferenced without null check — NPE on unknown loottable ID

Line 71:
```java
Loottable lt = hub.getLoottablesDAO().getLoottable(id);
List<LoottableItem> items = lt.getRandomItems(lt.getAmount());   // NPE if lt is null
```
If the loottable ID provided by the command sender does not exist, `getLoottable` returns `null` and the next line throws `NullPointerException` without any user feedback. A null guard with an error message must be added.

### 2. Infinite loop possible when `itemStacks.size() >= rows * 9`

Lines 91–97:
```java
int r = ThreadLocalRandom.current().nextInt(rows*9);
while (!(inv.getItem(r) == null)) {
    r = ThreadLocalRandom.current().nextInt(rows*9);
}
if (inv.getItem(r) == null) {
    inv.setItem(r, item);
}
```
The `while` loop keeps picking random slots until an empty slot is found. If the number of items to place is greater than or equal to `rows * 9` (i.e. the inventory is already full), the loop will spin indefinitely, hanging the server thread. A slot count guard must be added before entering the loop (e.g. break if all slots are occupied).

### 3. Redundant double check `inv.getItem(r) == null`

The `while` loop exits only when `inv.getItem(r) == null`. The `if (inv.getItem(r) == null)` check immediately after is therefore always `true` and is dead logic — the `setItem` call is unconditional.
