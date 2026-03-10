# Code Review — WoSSystems

> Reviewed: 2026-03-10 | ~216 Java files across all systems

---

## Critical

### 1. SQL Typo in `UnlockableDAO` — Silent Data Loss
**File:** `database/dao/UnlockableDAO.java:48`

`DELTE FROM playerdata_unlockables` is a typo for `DELETE FROM`. The query fails silently on every daily-reset cycle, leaving stale unlockable data in the database.

**Fix:** Change `DELTE` → `DELETE`.

---

### 2. `OfflinePlayer.getPlayer()` Without Null Check
**Files:**
- `systems/cooldowns/CooldownManager.java:75`
- `systems/cooldowns/cmd/subcmd/Give.java:64`

`OfflinePlayer.getPlayer()` returns `null` when the player is offline. Calling `triggerInteraction()` or similar methods with that null will throw a `NullPointerException`.

**Fix:**
```java
Player online = offlinePlayer.getPlayer();
if (online != null) triggerInteraction(online, ...);
```

---

### 3. Unsafe `CommandSender → Player` Cast in `debug.java`
**File:** `debug.java:44`

```java
Player p = (Player) commandSender; // no instanceof check
```

If the console or a command block runs this command, it throws `ClassCastException`.

**Fix:**
```java
if (!(commandSender instanceof Player p)) return true;
```

---

### 4. Array Index Access Without Bounds Check in `ActionHandler`
**File:** `utils/ActionHandler.java:52–88`

Multiple places access `parts[1]`, `parts[3]`, etc. directly after a `split(" ")` with no check on `parts.length`. A malformed action string (e.g. `"send_message"` with no argument) throws `ArrayIndexOutOfBoundsException`.

**Fix:** Always guard with `if (parts.length < N) { log error; return; }` before accessing index `N-1`.

Also, `split(" ")` on a string with consecutive spaces produces empty-string elements. Use `split("\\s+", 2)` when only the first token matters, or `split("\\s+")` generally.

---

### 5. Main-Thread DB Calls in `CooldownManager`
**File:** `systems/cooldowns/CooldownManager.java:26–66`

The `run()` method fires on the main thread every 20 ticks and performs:
- `getAllActiveCooldowns()` — full table scan
- `isCooldownExpired()` — per-cooldown DB query
- `removeCooldown()` — DB write per expired cooldown

This blocks the main thread proportionally to the number of active cooldowns.

**Fix:** Move the DB work to an async task; only schedule the interaction trigger back on the main thread with `Bukkit.getScheduler().runTask(...)`.

---

## Major

### 6. Race Condition in `EconomyDAO.currencyCache`
**File:** `database/dao/EconomyDAO.java`

`currencyCache` is declared `volatile` and loaded with a double-checked locking pattern, but the outer `if (currencyCache == null)` is not inside a synchronized block. Two threads can both pass the null check before either has finished loading, triggering two simultaneous DB reads and one overwriting the other's result.

**Fix:** Use a fully synchronized `getCurrencies()` method, or initialize via `computeIfAbsent` on a `ConcurrentHashMap`, or use a class-level lock.

---

### 7. N+1 Queries in `InteractionDAO.getInteractions()`
**File:** `database/dao/InteractionDAO.java:336–351`

```java
while (rs.next()) {
    interactions.add(getInteractionByID(interactionId)); // fires 4 more queries each
}
```

Loading N interactions executes 4N+1 queries (actions, holograms, particles, blocks, NPCs). With 50 interactions that is 200+ queries per load.

**Fix:** Load all child rows in bulk (`SELECT * FROM inter_actions WHERE id IN (...)`) and assemble in Java, or at minimum cache the full interaction list after first load.

---

### 8. Missing Null Check After `getCitem()` in `CraftingManager`
**File:** `professions/crafting/CraftingManager.java:41`

```java
hub.getCitemDAO().getCitem(r.resultCItemId()).clone(); // getCitem can return null
```

If a recipe references a custom item that doesn't exist, this throws `NullPointerException` and breaks the entire crafting system at startup.

**Fix:**
```java
ItemStack result = hub.getCitemDAO().getCitem(r.resultCItemId());
if (result == null) { plugin.getLogger().warning("Recipe " + r.getId() + " references unknown citem"); continue; }
```

---

### 9. `InteractionManager` Task — O(players × interactions × locations) Every 50 Ticks
**File:** `systems/interactions/InteractionManager.java:43–76`

The repeating task iterates all interactions, all their locations/NPCs, and all online players on every tick cycle. This scales quadratically and runs entirely on the main thread.

**Fix:** Flip the loop: for each online player, check nearby interactions using distance squared. Cache the interaction list and only reload when data changes rather than on a timer.

---

### 10. Broad `catch (Exception e)` Masking Real Errors
**Files:** `utils/ConditionHandler.java:94`, `systems/stats/StatsManager.java:26–38`, others

Catching the root `Exception` class hides unexpected errors (e.g. `ClassCastException` from a corrupt config value, `NumberFormatException` from a bad parameter) behind a generic "condition failed" return value.

**Fix:** Catch only the expected exception types (`SQLException`, `NumberFormatException`, etc.). Let others propagate or at least log them with full stack traces.

---

### 11. Inconsistent DAO Initialization — Some Don't Use `SchemaManager`
**Files:** `ProfileDAO.java`, `CitemDAO.java`, `DialogDAO.java`

Most DAOs now use `SchemaManager.syncTable()` for automatic column migration. These three still use raw `CREATE TABLE IF NOT EXISTS` strings, so new columns added to their data model won't be auto-migrated on existing databases.

**Fix:** Create annotated dataclasses for `ProfileDAO` and `CitemDAO` entries and migrate them to `SchemaManager.syncTable()`. `DialogDAO` uses an external API schema so it can stay manual, but should be documented as intentional.

---

### 12. `debug.java` Should Not Exist in Production
**File:** `debug.java`

The entire class is a developer command with no access guard beyond a raw permission node. It shouldn't be shipped or should be behind a `#ifdef`-equivalent (e.g. a `debug-mode: false` config flag with early return).

---

### 13. God Class: `WoSSystems.java`
**File:** `WoSSystems.java`

The main class is 550+ lines and is responsible for:
- All manager initialization
- All command registration (~30 commands)
- All event listener registration
- Database initialization
- Static utility methods

This makes it fragile to change and impossible to unit-test.

**Fix (incremental):** Extract `CommandRegistrar`, `ListenerRegistrar`, and `ManagerInitializer` helper classes called from `onEnable()`. Keep `WoSSystems` as a thin coordinator.

---

## Minor

### 14. Commented-Out Dead Code in `WoSSystems.java`
**File:** `WoSSystems.java:44, 105, 142–151, 189, 287, 294`

Blocks of commented-out PacketEvents and HologramHandler code. If these features are permanently removed they should be deleted; if they're pending re-introduction they should be tracked as a GitHub issue rather than left in source.

---

### 15. `playerRegions` Map Never Cleaned Up
**File:** `WoSSystems.java:136` (approx.)

`Map<UUID, String> playerRegions` is populated on join/move but there's no eviction call in `QuitListener`. Every player that logs out leaves an entry in this map forever (until server restart).

**Fix:** Add `playerRegions.remove(uuid)` in `QuitListener.leaveEvent()`.

---

### 16. `OfflinePlayer`/`Player` Lookup in `ChannelDAO` Methods
**File:** `database/dao/ChannelDAO.java:142, 176`

`Bukkit.getPlayer(playerUUID)` is called to send feedback messages, but the player might be offline (e.g. when force-joining a channel on login). The result isn't null-checked before `Utils.info(p, ...)`.

**Fix:** Guard with `if (p != null)` or accept a nullable player parameter and skip messaging when null.

---

### 17. `isTemp()` Called Twice Per `modifyUnlockable`
**File:** `database/dao/UnlockableDAO.java:99–100`

```java
if (isTemp(id)) { ... }
if (!isTemp(id)) { ... }
```

Two separate DB queries for the same value in the same method call.

**Fix:** `boolean temp = isTemp(id);` once, then use `temp` / `!temp`.

---

### 18. Typo in Error Message
**File:** `database/dao/CitemDAO.java:60`

`"Failed to intiialize CitemDAO table:"` — should be `initialize`.

---

### 19. `INSERT OR REPLACE` Is SQLite-Only Syntax
**File:** `database/dao/ProfileDAO.java:105`

`INSERT OR REPLACE INTO ...` is a SQLite extension. If the database ever migrates to MySQL or MariaDB, these queries break.

**Fix:** Use `INSERT INTO ... ON DUPLICATE KEY UPDATE ...` (MySQL) or abstract behind a helper that emits the correct upsert syntax based on the active driver. At minimum, add a comment flagging this as SQLite-specific.

---

### 20. Inefficient `getAllCitems()` Return in `CitemDAO`
**File:** `database/dao/CitemDAO.java:89–90`

```java
return new ArrayList<>(cache.keySet());
```

Creates a new list every call just to return the key set. If the caller only needs to iterate, return `Collections.unmodifiableSet(cache.keySet())` directly.

---

### 21. No Guarantee That Cache Is Ready on Player Join
**File:** `listeners/JoinListener.java:37`

`hub.loadPlayerData(uuid)` is submitted to `AsyncWriteQueue` (off main thread). If the player opens a GUI or runs a command in the first few milliseconds after join, the balance/stat cache may not be populated yet, causing fallback DB reads or returning default values.

**Fix:** Use a `CompletableFuture` from `loadPlayerData`, then unlock player interaction only after the future completes (or accept the fallback-to-DB path as intentional and document it).

---

### 22. Inconsistent Return Values on Error in DAOs
Across the DAOs, error paths return inconsistently:
- `getActionsForInteraction()` → empty list
- `getParticlesForInteraction()` → `null`
- `getHologramsForInteraction()` → `null`
- `getAllActivities()` → `null`

Callers have to guess whether to null-check or empty-check. Pick one convention (preferably empty collection, never null) and apply it everywhere.

---

### 23. Class File Named with Lowercase (`join.java`, `debug.java`)
Java convention requires class names and filenames to start with uppercase. Several command subclasses break this. Most IDEs and linters will flag this.

**Fix:** Rename to `Join.java`, `Debug.java`, etc. and update the class declarations.

---

### 24. Hard-Coded Blacklist in `ActionHandler`
**File:** `utils/ActionHandler.java:19`

```java
Arrays.asList("op", "gmc", "gamemode", ...)
```

This list is baked into the source. Server admins can't extend or override it without recompiling.

**Fix:** Move to `config.yml` under a `blocked-commands` key and load at startup.

---

### 25. Missing `@Override` on Some Listener Methods
A few event handler methods in older listener classes are missing `@Override`. While not a runtime bug, it means refactoring renames won't catch the broken listener.

---

## Summary

| Severity | Count |
|----------|-------|
| Critical | 5 |
| Major | 8 |
| Minor | 12 |
| **Total** | **25** |

### Top Priority Fixes
1. `UnlockableDAO` SQL typo (data loss)
2. `OfflinePlayer` null checks (NPE crash)
3. `debug.java` unsafe cast (crash on console)
4. `ActionHandler` bounds checks (crash on bad config)
5. `CooldownManager` async-ify DB calls (TPS impact)
