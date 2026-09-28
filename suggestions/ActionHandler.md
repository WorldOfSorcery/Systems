# ActionHandler

**Package:** `me.hektortm.woSSystems.utils`

## Suggestions

### 1. `eco` action block never calls `modifyCurrency` — balances never change
The GIVE, TAKE, SET, and RESET branches all call `ecoLog` but none of them actually calls `modifyCurrency`. Economy actions triggered via interactions silently do nothing to the player's balance.

### 2. `return` in sudo blacklist check should be `continue`
The early `return` on the blacklist check (when a player is on the sudo blacklist) aborts processing of **all** remaining actions, not just the current one. Replace with `continue` to only skip the blacklisted action.

### 3. `play_sound` crashes on bad config value
`Float.parseFloat` is called on the volume/pitch values without a try/catch. A malformed config entry will throw `NumberFormatException` and halt the entire action chain.
