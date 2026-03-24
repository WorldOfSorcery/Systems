# CraftingListener

**Package:** `me.hektortm.woSSystems.professions.crafting`

## Suggestions

### 1. `matchesShapelessRecipe` removes from an `ArrayList` during iteration — O(n²)
Items are removed from the `provided` list via `provided.remove(p)` (object equality scan) inside a loop over `required`. On an `ArrayList` this is O(n²). Use an `Iterator` with `iterator.remove()` or switch to a `LinkedList`.

### 2. Recipe list is loaded at construction and never refreshed
The `recipes` field is populated in the constructor but there is no reload mechanism. If recipes change at runtime (e.g. via a `/reload` command), the listener will continue using stale data until the server restarts.
