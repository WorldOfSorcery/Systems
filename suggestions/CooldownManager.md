# CooldownManager

**Package:** `me.hektortm.woSSystems.systems.cooldowns`

## Suggestions

### 1. Full-table DB scans every second
The `run()` method calls `getAllActiveCooldowns()` and `getAllActiveLocalCooldowns()` on every tick (every second). With many players these are full-table scans. Maintain an in-memory expiry map and only query the DB when the cache is cold.

### 2. `getCooldownDuration` issues a redundant DB query
`getCooldownDuration` queries the database even though the `preloadAll` cache already holds `Cooldown` objects with a `duration` field. Read the duration from the cache instead.
