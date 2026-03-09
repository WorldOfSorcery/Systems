# Citem System — Changelog v3
**Date:** 2026-03-09

This changelog covers the third round of improvements: comprehensive JSON storage for Web GUI support and dynamic (permission/condition-gated) item models.

---

## 1. Comprehensive JSON Storage (`CitemDAO`)

### Problem
The database previously only stored items as Base64 (`item_data`). The Web GUI couldn't read or edit items because Base64 is opaque and not human-readable.

### Solution: `item_json` column

A new `item_json TEXT` column was added to the `items` table (via silent `ALTER TABLE` at startup — safe to run on existing DBs). It stores a full JSON representation of every Citem that the Web GUI can read and write.

### JSON schema

```json
{
  "id": "my_sword",
  "material": "DIAMOND_SWORD",
  "name": "§6My Sword",
  "lore": ["§7A lore line"],
  "model": "wos:my_sword",
  "color": "#FF8800",
  "flags": ["HIDE_ATTRIBUTES"],
  "undroppable": true,
  "unusable": false,
  "custom_rarity": "LEGENDARY",
  "action_left": "some_interaction",
  "action_right": null,
  "action_placed": null,
  "action_consume": null,
  "cooldown_seconds": 5,
  "fire_resistant": false,
  "tooltip_id": null,
  "max_stack": 16,
  "food": { "nutrition": 4, "saturation": 2.0, "can_always_eat": false },
  "equippable": { "slot": "head" },
  "attributes": [
    { "attribute": "minecraft:generic.attack_damage", "amount": 5.0, "slot": "mainhand", "operation": "ADD_NUMBER" }
  ],
  "dynamic_lore": [
    { "type": "perm", "perm": "citem.vip", "text": "§6VIP Item" },
    { "type": "condition", "condition": "is_sneaking", "value": "", "parameter": "-", "text": "§cSneaking!" }
  ],
  "dynamic_model": [
    { "type": "perm", "perm": "citem.admin", "model": "admin_variant" },
    { "type": "condition", "condition": "has_stats_greater_than", "value": "kills", "parameter": "100", "model": "killer_variant" }
  ]
}
```

### Web GUI API methods (new in `CitemDAO`)

| Method | Description |
|---|---|
| `saveCitemFromJson(id, jsonStr)` | Create/update a Citem from raw JSON (Web GUI write path) |
| `getCitemJson(id)` | Return raw JSON string for a single item |
| `getAllCitemsJson()` | Return `JsonArray` of all items for the item list |

### Migration

On plugin startup, `migrateBase64ToJson()` is called asynchronously. Any row where `item_json IS NULL` is loaded from Base64, serialized to JSON, and saved back. Existing behavior is not affected.

### Read priority

`getCitem(id)` prefers `item_json` if available, falls back to `item_data` (Base64). Both paths remain supported.

---

## 2. Dynamic Item Models (`/citem model`)

### What changed

`/citem model` now supports permission- and condition-gated model overrides in addition to the existing static model set. Dynamic entries are stored as a JSON array in PDC key `dynamic-model` (`Keys.DYNAMIC_MODEL`).

At give time / `PlayerItemHeldEvent` / `PlayerJoinEvent`, `applyDynamicLore()` now also evaluates dynamic model entries. The **first matching entry** wins and overrides the static model for that player's copy of the item.

### Commands

| Command | Description |
|---|---|
| `/citem model <model_id>` | Set static model (existing behavior) |
| `/citem model perm <permission> <model_id>` | Add permission-gated model override |
| `/citem model cond <key> <value\|-> <param\|-> <model_id>` | Add condition-gated model override |
| `/citem model list` | List all dynamic model entries |
| `/citem model remove <index>` | Remove a dynamic model entry |
| `/citem model reset` | Remove the static model entirely |

**Permission:** `citem.modify.model`

### Example usages

```
/citem model my_sword                              — static model
/citem model perm citem.vip vip_sword              — VIP players see vip_sword model
/citem model cond has_stats_greater_than kills 100 killer_sword
/citem model cond is_sneaking - - sneaking_sword
/citem model list
/citem model remove 0
/citem model reset
```

### Entry format (JSON in PDC)

```json
// Permission-based
{"type":"perm","perm":"citem.vip","model":"vip_sword"}

// Condition-based
{"type":"condition","condition":"has_stats_greater_than","value":"kills","parameter":"100","model":"killer_sword"}
```

All model IDs are resolved under the `wos` namespace (i.e., `wos:<model_id>`).

---

## 3. Technical notes

- `CitemManager.applyDynamicLore()` now has three phases: (1) rarity prepend, (2) dynamic lore append, (3) dynamic model override
- `Model.java` completely rewritten to support sub-commands while keeping `args[0] = <model_id>` as the static set shortcut for backward compatibility
- `citems.yml`: `model` key restructured from scalar to mapping (`model.set`, `model.reset`, `model.perm.added`, `model.cond.added`, `model.removed`, `model.empty`); new `info.usage.model-perm`, `info.usage.model-cond`, `info.usage.model-remove` entries added
- `Keys.DYNAMIC_MODEL("dynamic-model")` was already present from a prior round
