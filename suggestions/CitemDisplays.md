# CitemDisplays

**Package:** `me.hektortm.woSSystems.systems.citems`

## Suggestions

### 1. `removeEntityAtLocation` uses floating-point location equality — will almost never match
The public method iterates all world entities and compares via `entity.getLocation().equals(location)`. Entity locations have sub-block precision, so this equality check will almost never succeed. The private `removeEntityAtLocationSafely` method already handles this correctly using `getNearbyEntities`. Replace the public method's implementation with the safe version.

### 2. Unreachable null check after `World.spawn`
`spawnItemDisplay` checks `if (itemDisplay == null)` after `World.spawn(...)`. `World.spawn` throws an exception rather than returning null, so this branch is unreachable dead code.
