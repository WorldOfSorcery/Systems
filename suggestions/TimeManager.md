# TimeManager

**Package:** `me.hektortm.woSSystems.time`

## Suggestions

### 1. Two load methods assign to the same `timeConfig` field — fragile ordering dependency
`loadConfiguration` (loads `activities.yml`) and `loadConfig` (loads `time-config.yml`) both assign to `timeConfig`. Whichever runs last wins. The `ticks` value used in `startInGameClock` is only valid because `loadConfig` happens to run after `loadConfiguration` in `onEnable`. This is an invisible ordering dependency that will cause subtle bugs if the call order ever changes. Use two separate fields.

### 2. `saveGameState` uses `e.printStackTrace()` instead of the plugin logger
Exceptions in `saveGameState` are printed to stderr via `printStackTrace`. Use `plugin.getLogger().severe(...)` for consistency and so the output is correctly tagged in the server log.
