# CooldownManager (logic additions)

**Package:** `me.hektortm.woSSystems.systems.cooldowns`

## Logic Issues

### 1. `onCooldownExpire` dereferences `getCooldown` result without a null check

Lines 108–109:
```java
Cooldown cd = hub.getCooldownDAO().getCooldown(cooldownId);
String endInt = cd.getEnd_interaction();
```
`getCooldown` may return `null` if the cooldown definition has been deleted from the DB after the active cooldown was created. `cd.getEnd_interaction()` then throws `NullPointerException`. A null guard is required after the `getCooldown` call.

### 2. `run()` is a `BukkitRunnable` body that immediately schedules async work — double indirection

The class extends `BukkitRunnable` and its `run()` method (line 47) is called by `runTaskTimer` (not async). Inside `run()`, it wraps everything in `Bukkit.getScheduler().runTaskAsynchronously(...)`. This means the timer fires on the main thread every second and does essentially nothing except submit another async task. If the async task takes longer than one second (DB is slow), multiple async instances will run concurrently, each querying `getAllActiveCooldowns` simultaneously, which may yield duplicate expiry removals and double-fire `onCooldownExpire` for the same player/cooldown pair. The task should be scheduled with `runTaskTimerAsynchronously` directly to avoid the double scheduling.

### 3. `isCooldownExpired` and `removeCooldown` are two separate DB round-trips inside a loop

Lines 61–63:
```java
if (hub.getCooldownDAO().isCooldownExpired(oP, cooldownId)) {
    hub.getCooldownDAO().removeCooldown(oP, cooldownId);
```
For each active cooldown, two separate queries are issued: one to check expiry, one to delete. A single `DELETE WHERE expiry < NOW()` query in the DAO would accomplish both atomically in one round-trip, eliminating the window where a cooldown could be considered expired by the check but already removed by another concurrent `removeCooldown` call.
