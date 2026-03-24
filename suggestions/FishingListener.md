# FishingListener

**Package:** `me.hektortm.woSSystems.professions.fishing`

## Suggestions

### 1. New `Random` instance created on every fish event
`getRandomRarity` creates a `new Random()` each call. Hold a single `Random` (or `ThreadLocalRandom`) instance as a class field instead.

### 2. Hardcoded rarity percentages
The rarity weights (50/30/15/4/0.7/0.27/0.03) are hardcoded. These should be read from config so they can be tuned without recompiling.

### 3. `Material.AIR` fallback can produce ghost items
When no fishing item is found, the item is set to `Material.AIR`. On some client/server versions this renders as a ghost item in the player's inventory. Cancelling the event or removing the entity is a safer fallback.
