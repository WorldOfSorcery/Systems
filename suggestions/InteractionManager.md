# InteractionManager

**Package:** `me.hektortm.woSSystems.systems.interactions`

## Suggestions

### 1. Dead condition in `"one"` match-type branch
When match type is `"one"`, `conditionList.isEmpty()` is checked as part of a condition after already confirming `!conditionList.isEmpty()` earlier in the same block. The second branch of the `||` is always false and will never execute.

### 2. `break` in behaviour block exits on first non-passing action
The `else break` in the behaviour check causes the interaction loop to exit on the first action whose behaviour is not `"continue"`, even if that action's conditions did not pass. This means later valid actions are silently skipped. Restructure so the break only fires after a condition-passing action completes.
