# CosmeticManager

**Package:** `me.hektortm.woSSystems.cosmetic`

## Suggestions

### 1. `openTitlesPage`, `openPrefixPage`, `openBadgePage` are near-identical
All three methods (~40 lines each) share the same permission-merge loop, lore-building pattern, and item-building logic. Extract a `buildCosmeticPage(CosmeticType, Material)` helper to eliminate the duplication.

### 2. Static shared inventory fields are mutated per-call — race condition
`titlesPage`, `prefixPage`, and `badgesPage` are static `Inventory` fields. Each `open*Page` call reassigns the static reference via `createInventory`. If two players open their cosmetic menu simultaneously, the second call overwrites the reference while the first player's click-handling is still using it. Each call should create and return a local inventory, not reassign a static field.
