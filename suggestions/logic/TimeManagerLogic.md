# TimeManager (logic additions)

**Package:** `me.hektortm.woSSystems.systems.time`

## Logic Issues

### 1. `loadConfiguration` and `loadConfig` both assign to `timeConfig` — ordering dependency causes data loss

`loadConfiguration` (line 276) loads `activities.yml` into `timeConfig`. `loadConfig` (line 231) loads `time-config.yml` into `timeConfig`, overwriting the activities config. `startInGameClock` then reads `timeConfig.getLong("tpm")` (line 168), which only exists in `time-config.yml`. The call order in `WoSSystems.onEnable` is:

```
timeManager.loadConfiguration();   // timeConfig = activities.yml
timeManager.loadGameStateConfig();
timeManager.loadGameState();
timeManager.loadConfig();          // timeConfig = time-config.yml (overwrites)
```

The `tpm` key is read correctly only because `loadConfig` happens to run last. Reversing the call order (or adding a `loadConfiguration` call after `loadConfig`) would break the clock by reading `tpm` from the wrong file. This is the existing issue in `TimeManager.md` — confirmed by source.

### 2. `createCalenderInventory` does not close after the month item null check

Line 86:
```java
if (monthMeta != null) {
    monthMeta.setDisplayName(...);
    monthItem.setItemMeta(monthMeta);
}
calendarInventory.setItem(4, monthItem);
```
If `monthMeta` is `null` (which should be impossible for a `CLOCK` item, but is theoretically possible), `monthItem` is placed into slot 4 without a display name. This is a minor visual bug but not a crash risk.

### 3. `createCalenderInventory` reads from the DB on the main thread for every calendar open

Line 96:
```java
for (Activity activity : hub.getTimeDAO().getAllActivities()) {
```
This is a synchronous DB call on the main thread, executed every time any player opens the calendar. If the DB is slow or the table is large, this causes a main-thread stall that the Bukkit watchdog may flag.

### 4. Day slot calculation places day 30 outside inventory bounds when using 54-slot inventory

Line 118:
```java
int slot = 8 + day;
```
For `day = 30`, `slot = 38`, which is within a 54-slot inventory (valid range 0–53). For `day <= maxDays (30)`, the maximum slot is `8 + 30 = 38`, which is fine. However, if `DAYS_IN_MONTH` were ever changed to a value greater than 45 (54 - 8 - 1 = 45), this calculation would write to slots ≥ 54, which `calendarInventory.setItem` silently ignores (guarded at line 151). The guard `if (slot < calendarInventory.getSize())` is present, so this is safe for the current constant but fragile if the constant is changed.
