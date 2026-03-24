# CitemListener

**Package:** `me.hektortm.woSSystems.systems.citems`

## Suggestions

### 1. Interaction cooldown consumed even when action is blocked by permissions
`triggerInteractionForDisplay` checks `isOnCooldown` and calls `updateCooldown` before the permission check. A right-click without edit permission still burns the player's cooldown window even though the action never ran. Move the cooldown check after the permission gate.

### 2. `item.setAmount(item.getAmount())` is a no-op
Setting an item's amount to its own current amount does nothing. The comment suggests this was meant to prevent a drop, but it has no effect. The correct approach is to cancel the event or clear the item slot explicitly.
