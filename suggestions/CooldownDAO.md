# CooldownDAO

**Package:** `me.hektortm.woSSystems.database.dao`

## Suggestions

### 1. Two DB queries for a single active-cooldown check
`isCooldownActive` and `getRemainingSeconds` both call `getPlayerStartTime` independently, resulting in two identical queries to the same row. Merge them into a single method that returns both the active state and the remaining time in one query.

### 2. `getCooldownDuration` queries DB when cache already has the answer
The `cache` field already holds `Cooldown` objects with a `duration` field. `getCooldownDuration` ignores the cache and issues a DB query. Read from the cache first, fall back to DB only on a cache miss.
