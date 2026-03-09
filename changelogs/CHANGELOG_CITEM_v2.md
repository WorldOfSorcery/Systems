# Citem System — Changelog v2
**Date:** 2026-03-09

This changelog covers the second round of improvements, building on v1. It focuses on deep integration with the existing Conditions system, replacement of the Bukkit rarity with the custom `Rarities` enum, and icon-driven lore.

---

## 1. Custom Rarity System (replacing Bukkit ItemRarity)

### What changed
`/citem rarity` now uses the project's own **`Rarities`** enum instead of Bukkit's built-in `ItemRarity`.

### `Rarities` enum — new `color` field + helpers
Each entry in `Rarities.java` now carries a chat-colour code:

| Rarity    | Color code | Icon   |
|-----------|-----------|--------|
| COMMON    | `§7`      | \uE006 |
| UNCOMMON  | `§a`      | \uE005 |
| RARE      | `§b`      | \uE004 |
| EPIC      | `§5`      | \uE003 |
| LEGENDARY | `§6`      | \uE002 |
| ANCIENT   | `§c`      | \uE001 |
| MYTHIC    | `§d`      | \uE000 |

New methods added to `Rarities`:
- `getColor()` — returns the colour prefix string
- `getLoreLine()` — returns `§<color><icon> <name>` (the formatted lore line)
- `fromName(String)` — static lookup by display name, case-insensitive

### How rarity is stored
- Stored in `PersistentDataContainer` under key `custom-rarity` (new `Keys.CUSTOM_RARITY`)
- **Not** written into visible lore at command time
- The lore line is **injected automatically** at the start of the visible lore by `applyDynamicLore()` on every give / item-held / login event

### Command: `/citem rarity <rarity|reset>`
- Accepts: `common`, `uncommon`, `rare`, `epic`, `legendary`, `ancient`, `mythic`, `reset`
- `reset` removes the PDC key entirely (no rarity line shown)
- **Permission:** `citem.modify.rarity`

---

## 2. Condition-based Dynamic Lore (`/citem lore cond`)

### What changed
Dynamic lore now supports the full **ConditionHandler** condition system — any condition available in interactions (stats, cooldowns, regions, currency, cosmetics, permissions, world, sneaking, citems, unlockables …) can gate a lore line.

### Storage format
Dynamic lore entries are stored as JSON in PDC key `dynamic-lore`. The array now supports two entry types:

```json
// Permission-based (existing)
{"type":"perm","perm":"citem.vip","text":"§6VIP Item"}

// Condition-based (new)
{"type":"condition","condition":"has_stats_greater_than","value":"kills","parameter":"100","text":"§aVerified Killer"}
```

A `-` in `value` or `parameter` is treated as empty/null.

### Commands
| Command | Description |
|---|---|
| `/citem lore cond add <key> <value\|-> <param\|-> <text...>` | Adds a condition-gated lore line |
| `/citem lore cond list` | Lists all dynamic entries (perm + cond) with type prefix |
| `/citem lore cond remove <index>` | Removes entry at index |

**Permission:** `citem.modify.lore` (same as regular lore)

### Example usages
```
/citem lore cond add has_permission citem.vip - §6VIP Only
/citem lore cond add has_stats_greater_than kills 50 §aKiller
/citem lore cond add is_sneaking - - §cSneaking!
/citem lore cond add has_currency gold 100 §eRich
/citem lore cond add is_in_region pvp_zone - §cDanger Zone
```

### Unified list/remove
`/citem lore perm list`, `/citem lore perm remove`, `/citem lore cond list`, and `/citem lore cond remove` all operate on the **same shared array**. Both commands show all entries (perm and condition) to avoid confusion. Entry type is shown with a colour prefix: `§6[perm]` or `§d[cond]`.

---

## 3. `applyDynamicLore` — updated resolution order

`CitemManager.applyDynamicLore(Player, ItemStack)` now:

1. **Prepends** the custom rarity line (`§<color><icon> <name>`) as position-0 lore if `CUSTOM_RARITY` PDC key is set
2. **Appends** visible dynamic entries — evaluating each as `perm` or `condition` against the player
3. **Returns** the modified item copy (DB item is never mutated)

`ConditionHandler` is now instantiated inside `CitemManager` so dynamic lore resolution has full access to all condition types without extra dependencies.

---

## 4. Enhanced `/citem info`
- Custom rarity now shown using `Rarities.getLoreLine()` (coloured with icon) instead of Bukkit rarity
- Dynamic lore entries display with type-colour prefix: `§6[perm]` or `§d[cond]`
- Condition entries show condition key, value, and parameter inline

---

## 5. Technical / cleanup notes
- `Keys.CUSTOM_RARITY` (`"custom-rarity"`) added to `Keys` enum
- `Permissions.CITEM_RARITY` already existed and is reused
- Old Bukkit `ItemRarity` import removed from `Info.java` and `Rarity.java`
- `Lore.java` refactored: `handlePermLore` and `handleCondLore` share `getDynEntries()`, `saveDynEntries()`, `listAllDynEntries()`, and `removeDynEntry()` helpers to avoid duplicate code
- `citems.yml` updated with all new keys: `error.rarity.invalid`, `lore.cond.*`, `info.usage.lore-cond`, `info.usage.lore-cond-add`
