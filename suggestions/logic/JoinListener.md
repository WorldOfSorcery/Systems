# JoinListener

**Package:** `me.hektortm.woSSystems.core`

## Logic Issues

### 1. Player data loaded async but handlers may use it synchronously before it arrives

`hub.loadPlayerData(uuid)` is dispatched as an async task (line 36). Any handler that fires shortly after `PlayerJoinEvent` — including `ChannelListener`, `BossBarManager`, etc. — may call `getPlayerCurrency` or `getPlayerStatValue` before the async load has finished, resulting in cache misses that silently return `0` rather than the player's real values. The join sequence should either wait for the load to complete before continuing, or every cache-read site must be able to tolerate a cold-cache return value of `0` as a temporary state.

### 2. `autoJoin` and `forceJoin` called before player data is in memory

`plugin.getChannelManager().autoJoin(player)` and `forceJoin(player)` are invoked synchronously (lines 25–29) before the async `loadPlayerData` task starts. `forceJoin` calls `hub.getChannelDAO().isInChannel(...)` which performs a DB read to determine whether the player is already subscribed. Because no player data is cached yet, the DAO falls back to a live DB query every time — which is fine for correctness but defeats the caching strategy. More critically, if `isInChannel` is later changed to use a cache-only path, it would return incorrect results for every joining player.
