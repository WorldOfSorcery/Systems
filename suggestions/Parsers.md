# Parsers

**Package:** `me.hektortm.woSSystems.utils`

## Suggestions

### 1. Two parallel implementations of unicode parsing with no explanation
`parseUni` (instance method, uses if/else chain) and `parseUniStatic` (static method, uses array indexing) implement the same conceptual operation with different strategies. Changes to the character mapping must be applied twice or they'll diverge. Consolidate into one implementation.

### 2. `stringToLocation` uses `System.err.println` and `printStackTrace` instead of the plugin logger
Exceptions in `stringToLocation` are printed directly to stderr. Use `plugin.getLogger().severe(...)` so the output is properly tagged in the server log.
