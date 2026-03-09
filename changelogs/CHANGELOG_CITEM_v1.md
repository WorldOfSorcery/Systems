# Citem System Changelog

## [Unreleased] — 2026-03-09

### Bug Fixes
- **Fixed typo** in `CitemCommand.java`: subcommand was registered as `"echant"` instead of `"enchant"`, meaning `/citem enchant` never worked.

---

### New Features

#### `/citem attribute <add|remove|clear|list>` — Item Attribute Modifiers
New subcommand to attach vanilla attribute modifiers to items.

- `/citem attribute add <attribute> <amount> [slot] [operation]`
  - Supported attributes: `attack_damage`, `attack_speed`, `armor`, `armor_toughness`, `max_health`, `knockback_resistance`, `movement_speed`, `luck`, `max_absorption`, `attack_knockback`, `reach`
  - Supported slots: `mainhand`, `offhand`, `head`, `chest`, `legs`, `feet`, `armor`, `any`
  - Supported operations: `add` (flat value), `multiply_base` (% of base), `multiply_total` (% of total)
  - Defaults to slot `any` and operation `add` if not specified
- `/citem attribute remove <attribute>` — removes all modifiers for that attribute
- `/citem attribute clear` — removes all attribute modifiers from the item
- `/citem attribute list` — displays all active attribute modifiers

**Permission:** `citem.modify.attribute`

---

#### `/citem rarity <common|uncommon|rare|epic|reset>` — Item Rarity
Sets the item rarity, which changes the colour of the item name:
- `COMMON` — white
- `UNCOMMON` — green
- `RARE` — aqua
- `EPIC` — magenta
- `reset` — reverts to default rarity for the material

**Permission:** `citem.modify.rarity`

---

#### `/citem food <nutrition> <saturation> [can_always_eat]` — Food Properties
Makes any item edible using the Paper data component API.

- `nutrition` — integer, how many hunger points restored
- `saturation` — float, how much saturation restored
- `can_always_eat` — `true`/`false` (default `false`), whether the item can be eaten when not hungry
- `/citem food remove` — removes the food component from the item

**Permission:** `citem.modify.food`

---

#### `/citem maxstack <1-99|reset>` — Custom Max Stack Size
Overrides the default max stack size using `DataComponentTypes.MAX_STACK_SIZE`.

- Allows items to stack up to 99 or to be forced to stack only 1 (unstackable)
- `reset` — reverts to the material's default stack size

**Permission:** `citem.modify.maxstack`

---

#### `/citem delete <id>` — Delete from Database
Deletes a saved Citem from the database permanently.

- Validates that the ID exists before deleting

**Permission:** `citem.delete` *(existing permission)*

---

#### `/citem list` — List All Saved Citems
Displays all registered Citem IDs along with the total count.

**Permission:** `citem.list`

---

#### `flag add/remove fireresist` — Fire Resistance Flag
New flag using `DataComponentTypes.FIRE_RESISTANT` (Paper 1.21.4+ data component):

- `/citem flag add fireresist` — makes the item fire/lava resistant (won't burn in fire)
- `/citem flag remove fireresist` — removes fire resistance

---

#### Permission-Gated Dynamic Lore (`/citem lore perm`)
Lore lines that are only visible to players with a specific permission node. These are stored in the item's `PersistentDataContainer` as JSON (`dynamic-lore` key) and resolved dynamically.

- `/citem lore perm add <permission.node> <text>` — adds a lore line visible only to players with that permission
- `/citem lore perm list` — lists all dynamic lore entries with their permission nodes and index
- `/citem lore perm remove <index>` — removes a dynamic lore entry by index

**Resolution happens at:**
1. Item give via `/cgive` — lore resolved for the receiving player
2. `PlayerItemHeldEvent` — re-resolved when the player switches to the item
3. `PlayerJoinEvent` — all dynamic lore in inventory re-resolved on login

This means a player's lore will update automatically if their permissions change, on next login or when they re-select the item.

---

### Improvements

#### Enhanced `/citem info`
The info command now displays significantly more data:
- Material type
- Rarity (if set)
- Custom max stack size (if different from default)
- Fire resistance status
- Food properties (nutrition, saturation, can always eat)
- All flags in a clean list format
- Attribute modifiers with slot and operation
- Dynamic lore entries with their permission nodes
- Styled with a separator line header/footer

Also fixed missing `isPlayer()` check that could crash when run from console.

#### `CitemManager.applyDynamicLore(Player, ItemStack)`
New public method that filters permission-gated lore entries from the `DYNAMIC_LORE` PDC key and appends visible ones to the item's lore. Used internally by `giveCitem()` and the listener events.

#### `CitemDAO.deleteCitem(String id)`
New database method to permanently delete a saved Citem by ID.

---

### Technical Changes
- `Keys.java`: Added `DYNAMIC_LORE` (`"dynamic-lore"`) and `CONSUME_ACTION` / `COOLDOWN` keys for future use
- `Permissions.java`: Added `CITEM_ATTRIBUTE`, `CITEM_RARITY`, `CITEM_FOOD`, `CITEM_MAXSTACK`, `CITEM_LIST`, `CITEM_MATERIAL` permission entries
- `citems.yml`: Added messages for all new commands, error keys, and the new flag
- All new subcommands follow the existing `SubCommand` pattern with proper permission checks and player-only guards
