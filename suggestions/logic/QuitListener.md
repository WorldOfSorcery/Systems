# QuitListener

**Package:** `me.hektortm.woSSystems.core`

## Logic Issues

### 1. Region bossbar is not removed on quit

`onEnable` registers both `bossBarManager.removeBossBar(p)` and `regionBossBarManager.removeBossBar(p)` in the `onDisable` loop (line 306–308), and `createBossBar` is called for both on join. However, `leaveEvent` (line 40) only calls `plugin.getBossBarManager().removeBossBar(p)` — it never calls `plugin.getRegionBossBarManager().removeBossBar(p)`. The region bossbar therefore leaks for every disconnecting player and is only cleaned up on plugin disable, not on individual quits.

### 2. `challengeQueue` contains-then-remove is redundant

Lines 36–38:
```java
if (coinflip.challengeQueue.containsKey(p.getUniqueId())) {
    coinflip.challengeQueue.remove(p.getUniqueId());
}
```
`Map.remove` is a no-op when the key is absent, so the `containsKey` guard adds two map lookups where one would do. This is not a correctness bug on its own, but in a concurrent context (the map is also accessed from `InventoryClickListener`) the window between `containsKey` and `remove` allows a race where another thread inserts the same key after the check passes but before the remove executes, silently losing that entry. Use `challengeQueue.remove(p.getUniqueId())` directly.
