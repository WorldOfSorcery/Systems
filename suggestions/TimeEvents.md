# TimeEvents

**Package:** `me.hektortm.woSSystems.time`

## Suggestions

### 1. `checkForActivity` queries all activities from the DB on every tick
The activity list is fetched from the DAO every in-game minute. Cache the list at startup and refresh only when a reload is triggered.

### 2. Dead code — empty `if (!dateMatches)` block
There is an empty `if (!dateMatches) {}` block that does nothing. Remove it.
