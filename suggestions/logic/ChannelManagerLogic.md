# ChannelManager (logic additions)

**Package:** `me.hektortm.woSSystems.systems.channels`

## Logic Issues

### 1. `getFormattedMessage` checks `item == null` after already dereferencing it

Lines 402–433:
```java
ItemStack item = sender.getInventory().getItemInMainHand();
ItemMeta meta = item.getItemMeta();       // ← dereference
String itemName;
if (item.hasItemMeta() && meta.hasDisplayName()) { ... }
...
if (item == null || item.getType() == Material.AIR) {  // ← null check too late
```
`item.getItemMeta()` is called and `item.hasItemMeta()` / `meta.hasDisplayName()` are evaluated before the `item == null` guard on line 433. `getItemInMainHand()` never returns `null` (it returns `AIR`), so the null check is dead code — but it creates a false sense of safety and masks the real risk: `meta` can be `null` for `AIR` items. `meta.hasDisplayName()` would throw `NullPointerException` in that case. The AIR check must be performed before calling `getItemMeta()`.

### 2. `viewItem` mutates the single shared `itemPreview` inventory — race condition

Line 537–539:
```java
public Inventory viewItem(ItemStack item) {
    itemPreview.setItem(4, item);
    return itemPreview;
}
```
`itemPreview` is a single instance-level field. `getFormattedMessage` is called on `AsyncPlayerChatEvent`, which fires on a background thread. If two players chat simultaneously, both calls set slot 4 of the same inventory to different items before the click handler reads it, meaning a player clicks `[item]` and sees the wrong player's item. Additionally, the click UUID stored in `plugin.getClickActions()` maps to the *same inventory object*, so all concurrent chat messages share the same backing inventory — the last writer wins.

Each call to `viewItem` should create a fresh `Inventory` instance rather than reusing the shared one.

### 3. `loadChannels` is called both from the constructor and from `onEnable` — channels loaded twice

The `ChannelManager` constructor (line 55) calls `loadChannels()`. `WoSSystems.onEnable` then calls `channelManager.loadChannels()` again at line 280. Every channel is therefore loaded into the `channels` map twice (the second call puts the same keys again, which is idempotent for a `HashMap` but performs redundant DB queries on every startup). The constructor call should be removed and loading should be done explicitly from `onEnable`.
