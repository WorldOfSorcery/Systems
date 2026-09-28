# ParticleHandler

**Package:** `me.hektortm.woSSystems.systems.interactions`

## Suggestions

### 1. Two near-identical `switch` blocks for NPC vs block particles
`spawnParticles` contains two nearly identical switch blocks differing only in which spawn helper is called. Extract the target-specific logic into a strategy or pass a spawn lambda as a parameter to eliminate the duplication.

### 2. Hex color parsing duplicated three times
`spawnRedstoneParticles`, `spawnRedstoneNPCParticles`, and `spawnRedstoneParticleCircle` each repeat the same `colorHex.substring(1,3)` / `parseInt` pattern. Extract a `parseHexColor(String hex)` helper.

### 3. No bounds check on `colorHex` — `StringIndexOutOfBoundsException`
If `colorHex` is shorter than 7 characters (e.g. a misconfigured value), `substring(1,3)` etc. will throw `StringIndexOutOfBoundsException`. Validate the length before parsing.
