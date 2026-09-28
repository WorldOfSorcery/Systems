# BossBarManager & RegionBossBar

**Package:** `me.hektortm.woSSystems.time` / `me.hektortm.woSSystems.regions`

## Suggestions

### 1. `applyColorAndNegativeSpace` is duplicated across both classes
Both `BossBarManager` and `RegionBossBar` contain an identical `applyColorAndNegativeSpace` method (~70 lines) and identical `HEX_COLOR_PATTERN` / `LEGACY_COLOR_PATTERN` regex fields. Extract this logic to a shared utility class (e.g. `BossBarUtil`) to avoid the duplication and ensure any fix is applied in both places.
