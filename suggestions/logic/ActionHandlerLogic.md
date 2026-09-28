# ActionHandler (logic additions)

**Package:** `me.hektortm.woSSystems.utils`

## Logic Issues

### 1. `eco` block falls through to console-command dispatch for every actionType

Lines 176–207:
```java
if(cmd.startsWith("eco")) {
    ...
    if (actionType.equalsIgnoreCase("give")) { ... }
    if (actionType.equalsIgnoreCase("take")) { ... }
    if (actionType.equalsIgnoreCase("set"))  { ... }
    if (actionType.equalsIgnoreCase("reset")){ ... }
}
if (cmd.startsWith("close_gui")) { ... }

// fallthrough:
if (sourceType == SourceType.DIALOG) ...
else Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsedCommand);
```
After the `eco` block, there is no `continue` statement. Execution falls through to the final `dispatchCommand` call, which dispatches the raw `eco give @p coins 100` string as a console command on every `eco` action. This means every economy action fires twice: once via `ecoLog` and once as an unrecognised console command. A `continue` must be added at the end of the `eco` block.

### 2. `cooldown give` with `null` key silently does nothing with no log

Lines 124–132:
```java
if (parts[1].contains("give") && parts[4].contains("%local%")) {
    if (key != null) {
        hub.getCooldownDAO().giveLocalCooldown(player, parts[3], key);
        ...
        continue;
    }
}
```
When `key` is `null` (e.g. the action is triggered from a loottable or dialog), the inner `if (key != null)` block is skipped silently. The cooldown is never granted, and execution falls through to the final `dispatchCommand` call, dispatching `cooldown give @p id %local%` as a raw console command. There should be a `continue` or `break` (or at minimum a warning log) in the `else` branch when `key` is null to prevent the raw dispatch.
