# ChannelListener

**Package:** `me.hektortm.woSSystems.systems.channels`

## Logic Issues

### 1. `sendMessage` accesses Bukkit API from async thread

`onPlayerChat` is fired on `AsyncPlayerChatEvent` (line 26). Inside `channelManager.sendMessage`, the code calls `player.getLocation()`, `Bukkit.getPlayer(recipientUUID)`, and `recipient.sendMessage(chatComponent)` — all Bukkit API calls that are not safe to call from an async thread. Specifically, `sendMessage` on a `Player` object is safe from async contexts in Paper, but `player.getLocation()` and `Bukkit.getPlayer()` are not thread-safe because they access entity state on the main thread's tick. This can produce stale data or cause rare ConcurrentModificationExceptions. The entire message delivery should be wrapped in `Bukkit.getScheduler().runTask(plugin, ...)`.

### 2. Return without cancelling event when `core_mail` gate triggers

Line 31–33:
```java
if (unlockableManager.getPlayerTempUnlockable(player, "core_mail")) {
    return;
}
```
When the `core_mail` unlockable is active, the handler returns early *without* calling `event.setCancelled(true)`. The raw chat event is therefore delivered to all listeners (including vanilla chat broadcast), even though the intent is to suppress the message. `event.setCancelled(true)` must be called before returning.
