# ChannelManager

**Package:** `me.hektortm.woSSystems.channels`

## Suggestions

### 1. New `NicknameManager` constructed on every chat message
A `new NicknameManager(...)` is created inside `getFormattedMessage` on every chat event. This is very wasteful — inject and reuse the instance already created during startup.

### 2. Shared `itemPreview` inventory is mutated concurrently
The `itemPreview` inventory is a single static instance. If two players trigger `viewItem` at the same time, the second call overwrites the inventory contents while the first player's click-handling is still reading them. Each call should create its own inventory instance.

### 3. `getFormattedMessage` is doing too much
The method is ~120 lines and mixes display formatting, database I/O, and item-link construction. Split into focused helpers (e.g. `buildItemLink`, `resolveDisplayName`) to make it testable and maintainable.

### 4. `saveChannels` is a silent no-op
The method body only logs a comment and returns. If anything calls this expecting persistence, it will silently lose data.
