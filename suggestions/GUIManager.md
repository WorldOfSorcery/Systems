# GUIManager

**Package:** `me.hektortm.woSSystems.systems.guis`

## Suggestions

### 1. `handleClickActions` mutates a cached list — corrupts future lookups
`config.getGlobal_actions()` returns the list directly from the DAO cache. `handleClickActions` calls `addAll` on this list, permanently mutating the cached data. Every subsequent call for that config will see the accumulated mutations. Copy into a new list before calling `addAll`:
```java
List<InteractionAction> actions = new ArrayList<>(config.getGlobal_actions());
actions.addAll(slotActions);
```

### 2. Silent material fallback hides misconfigured GUIs
When a material is invalid, `buildItem` silently falls back to `PAPER` with no log warning. A `plugin.getLogger().warning(...)` call here would make misconfigured GUIs immediately obvious in the console.
