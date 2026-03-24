# PlaceholderResolver

**Package:** `me.hektortm.woSSystems.utils`

## Suggestions

### 1. NPE when `getItemMeta()` returns null in citems namespace
`resolvePlaceholders` calls `hub.getCitemDAO().getCitem(id)` and then `item.getItemMeta()` without a null check on the returned meta. Certain material types can return null from `getItemMeta()`. If this happens, the next line that calls methods on `meta` will throw `NullPointerException`. Add a null guard before accessing meta methods.
