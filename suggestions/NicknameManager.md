# NicknameManager

**Package:** `me.hektortm.woSSystems.channels`

## Suggestions

### 1. `reservedNicks` field is `public` — breaks encapsulation
`InventoryClickListener` accesses `reservedNicks` directly as a public field. Expose it through a method (e.g. `isReserved(String nick)`) to keep the internal structure hidden and the logic in one place.

### 2. Reserved nick lookup duplicated across two classes
The `anyMatch` stream over `reservedNicks.values()` is performed in both `NicknameManager` and `InventoryClickListener`. Extract this into a single `isReserved(String)` method on `NicknameManager`.
