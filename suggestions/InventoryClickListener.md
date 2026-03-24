# InventoryClickListener

**Package:** `me.hektortm.woSSystems.listeners`

## Suggestions

### 1. No null check on `clickedItemMeta` — NPE on empty slot click
The cosmetic handling block reads `clickedItemMeta` (from `getItemMeta()`) and immediately calls methods on it without a null check. Clicking an empty slot returns a null meta and throws `NullPointerException`. Add a null guard before accessing any meta methods.

### 2. Title branch hardcodes type string as `"Prefix"`
The success message in the title equip branch passes `"Prefix"` as the cosmetic type string even when equipping a title. This produces incorrect feedback to the player.

### 3. `getCurrentCosmeticId` called twice unnecessarily
The title-equality check calls `getCurrentCosmeticId` twice in the same expression instead of storing the result in a local variable first.
