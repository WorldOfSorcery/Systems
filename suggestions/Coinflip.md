# Coinflip

**Package:** `me.hektortm.woSSystems.systems.economy.commands`

## Suggestions

### 1. `challengeQueue` is a `HashMap` accessed from multiple threads
`challengeQueue` is accessed from `InventoryClickListener`, `QuitListener`, and the async chat event handler. Plain `HashMap` is not thread-safe. Replace with `ConcurrentHashMap`.

### 2. No rollback if currency deduction fails
`resolveChallenge` deducts currency from both players before the coin flip. If either `modifyCurrency` call fails silently (it returns void), there is no rollback mechanism — a player can lose currency without a winner being determined. Consider checking balances and locking the transaction before deducting.

### 3. `randomInt(2, 1)` — confusing argument order, use `nextBoolean()` instead
The coin flip calls `randomInt(2, 1)` which produces either 1 or 2. The argument order is unintuitive. Replace with `ThreadLocalRandom.current().nextBoolean()` for clarity.
