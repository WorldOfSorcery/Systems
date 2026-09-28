# LoottableManager

**Package:** `me.hektortm.woSSystems.systems.loottables`

## Suggestions

### 1. No null check on `getLoottable(id)` return value
`lt.getRandom()` is called immediately after `getLoottable(id)` without checking if `lt` is null. If an unknown ID is passed, this throws `NullPointerException`. Add a null guard and send an error message or log a warning.

### 2. Unsafe cast from `OfflinePlayer` to `Player`
A parameter typed as `OfflinePlayer` is directly cast to `Player`. If an offline player is ever passed (which the type signature explicitly allows), this throws `ClassCastException`. Change the parameter type to `Player` directly since all call sites pass an online player.
