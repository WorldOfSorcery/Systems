# RegionHandler

**Package:** `me.hektortm.woSSystems.regions`

## Suggestions

### 1. `onPlayerMove` runs full WorldGuard region lookup on every movement packet
`onPlayerMove` fires hundreds of times per second per player and calls `updateRegion` which constructs a `BlockVector3`, queries the WorldGuard region container, and iterates all applicable regions on the main thread every time. Throttle this to only fire when the player crosses a block boundary by comparing their previous and current block coordinates.

### 2. `leaveInteraction` flag is defined but never read
The `leaveInteraction` field is declared and set but its value is never consumed anywhere. Either wire it up or remove it.
