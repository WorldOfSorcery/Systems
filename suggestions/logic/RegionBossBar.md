# RegionBossBar

**Package:** `me.hektortm.woSSystems.systems.regions`

## Logic Issues

### 1. Null check on `bossBar` is after the first use — NPE on empty-string update

In `updateBossBar` (lines 39–44):
```java
if (regionName.isEmpty()) {
    bossBar.name(Component.text(""));   // ← used before null check
    return;
}

if (bossBar == null) {
    return;
}
```
`bossBar` is retrieved via `bossbars.get(p.getUniqueId())` (line 37) and may be `null` if `createBossBar` was never called for this player. The early-return branch for an empty `regionName` calls `bossBar.name(...)` before the null check, throwing `NullPointerException` whenever `updateBossBar` is called with an empty string for a player who has no bossbar. The null check must be moved before the first use of `bossBar`.
