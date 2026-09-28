# ProfileManager

**Package:** `me.hektortm.woSSystems.systems.profiles`

## Logic Issues

### 1. `editProfile` and `viewProfile` are static fields — concurrent opens corrupt each other

Lines 25–26:
```java
private static Inventory editProfile;
private static Inventory viewProfile;
```
Both fields are reassigned by `openEditProfile` and `openViewProfile` respectively. If two players open their profiles simultaneously (or one opens their own while another opens a viewed profile), the second call overwrites the static field while the first player's inventory is still open and the click/close handlers are still referencing the old object via `manager.getEditProfile()` / `manager.getViewProfile()`. The handlers use `inv.equals(manager.getEditProfile())` to identify the inventory — once the field is overwritten, neither player's inventory matches and all subsequent clicks are silently ignored. Each call must create and store the inventory per-player, not as a shared static.

### 2. `createPlayerHead` calls `player.getName()` on an `OfflinePlayer` — can return null

Line 118:
```java
meta.setDisplayName("§f"+player.getName());
```
`OfflinePlayer.getName()` returns `null` for players who have never logged in (or whose name has not been cached). `"§f" + null` produces `"§fnull"` and `meta.setDisplayName("§fnull")` will display "null" as the skull name in-game.
