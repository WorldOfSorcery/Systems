# CraftingListener (logic additions)

**Package:** `me.hektortm.woSSystems.systems.professions.crafting`

## Logic Issues

### 1. `matchesShapelessRecipe` modifies an `ArrayList` during iteration — `ConcurrentModificationException`

Lines 79–90:
```java
for (String neededId : required) {
    for (ItemStack p : provided) {
        if (isExactCItem(p, neededId)) {
            found = true;
            provided.remove(p);   // ← modifies `provided` while iterating it
            break;
        }
    }
```
The inner `for-each` iterates the `provided` ArrayList. `provided.remove(p)` modifies the list while the inner loop is still in scope. Although the `break` exits the inner loop immediately after the remove, Java's `ArrayList` iterator marks the list as structurally modified, and on the next outer iteration when the inner for-each restarts, a `ConcurrentModificationException` is thrown. The fix is to use an explicit `Iterator` with `iterator.remove()` or to remove by index.

### 2. `onCraft` cancels all vanilla crafting — prevents normal (non-CRecipe) crafting

Lines 133–136:
```java
if (match == null) {
    event.setCancelled(true);
    return;
}
```
If `findMatchingRecipe` returns `null`, the craft is cancelled entirely. This means any vanilla recipe that the server still has registered (e.g. a wooden sword) is blocked if the crafting grid happens to reach `onCraft` without matching a custom recipe. `PrepareItemCraftEvent` already sets the result to `null` for non-matching recipes (line 122), which prevents the craft from completing in normal play — the `setCancelled(true)` in `onCraft` for `match == null` is therefore overly aggressive and blocks edge cases where the player might bypass `PrepareItemCraftEvent` (e.g. via shift-click restocking). More importantly, it blocks all vanilla crafts entirely if a player ever has a custom item anywhere in the grid that doesn't match a recipe.
