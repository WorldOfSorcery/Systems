# ConditionHandler

**Package:** `me.hektortm.woSSystems.utils`

## Logic Issues

### 1. `has_not_currency` uses `<=` instead of `<` — wrong semantics

Line 131:
```java
case "has_not_currency": return hub.getEconomyDAO().getPlayerCurrency(...) <= (parameter != null ? Long.parseLong(parameter) : 0);
```
`has_not_currency` with a parameter of `100` should mean "the player does **not** have at least 100 of this currency", i.e. the balance is **strictly less than** 100. Using `<=` means the condition also passes when the player's balance is **exactly** 100, making it asymmetric with `has_currency` (which uses `>=`). The correct operator is `<`.

### 2. `is_in_region` / `is_not_in_region` read from a `HashMap` accessed by multiple threads without synchronisation

Lines 110–116:
```java
Map<UUID, String> playerRegions = plugin.getPlayerRegions();
String regionId = playerRegions.get(player.getUniqueId());
```
`playerRegions` is a plain `HashMap` in `WoSSystems`. It is written by `RegionHandler.onPlayerMove` (main thread) and by `QuitListener.leaveEvent` (main thread), but conditions are evaluated from interaction triggers that can be dispatched from async contexts (e.g. `AsyncPlayerChatEvent`). A `HashMap` is not thread-safe; concurrent reads and writes can cause infinite loops or corrupt results. The field should be a `ConcurrentHashMap`.

### 3. `Condition.getParameter()` parsed without null check for numeric conditions

Lines 101–103:
```java
case "has_stats_greater_than": return stats.getPlayerStat(...) > Long.parseLong(condition.getParameter());
case "has_stats_less_than":    return stats.getPlayerStat(...) < Long.parseLong(condition.getParameter());
case "has_stats_equal_to":     return stats.getPlayerStat(...) == Long.parseLong(condition.getParameter());
```
If a condition is misconfigured with a `null` or non-numeric `parameter`, `Long.parseLong(null)` throws `NumberFormatException` / `NullPointerException`. The surrounding `try/catch (Exception e)` catches these and returns `false`, silently masking the misconfiguration. The global catch is a reasonable safety net, but the lack of a specific guard means there is no log message that identifies *which* condition was misconfigured, making debugging difficult.
