# NicknameManager (logic additions)

**Package:** `me.hektortm.woSSystems.systems.channels`

## Logic Issues

### 1. `getNickRequests()` returns an unmodifiable view, but `InventoryClickListener` holds a direct reference to the underlying map

In `WoSSystems.onEnable` line 490:
```java
new InventoryClickListener(..., nickManager.getNickRequests(), nickManager, ...)
```
`getNickRequests()` returns `Collections.unmodifiableMap(nickRequests)`. `InventoryClickListener` stores this as `Map<UUID, String> nickRequests` and reads it on clicks. However, the real `nickRequests` map is mutated directly by `requestNicknameChange`, `approveNicknameChange`, and `denyNicknameChange` without synchronisation. Because `AsyncPlayerChatEvent` can fire concurrently and new `NicknameManager` instances are constructed on every chat event (see `ChannelManager.getFormattedMessage`), there is no guarantee that the `nickRequests` map is in a consistent state when read from the click handler.

### 2. `InventoryClickListener` proceeds with approval even when the nickname is reserved

Lines 196–207 in `InventoryClickListener`:
```java
if (isReserved) {
    UUID reservedBy = ...;
    if (reservedBy != null && !reservedBy.equals(requesterUUID)) {
        nickManager.denyNicknameChange(p);   // deny is called...
    }
}

// Handle left and right clicks
if (ct == ClickType.LEFT || ct == ClickType.SHIFT_LEFT) {
    nickManager.approveNicknameChange(p.getUniqueId());   // ...but approve is also called
```
When a nickname is reserved by another player, `denyNicknameChange` is called — but execution continues into the left-click branch which calls `approveNicknameChange` on the same UUID. The deny is immediately overridden by the approve. A `return` is required after the `denyNicknameChange` call.
