# WoSSystems

**Package:** `me.hektortm.woSSystems`

## Suggestions

### 1. NPE risk when WebhookServer constructor throws
`start()` is called unconditionally after the constructor in a try/catch block. If the constructor throws, `start()` still executes on a partially constructed object. Guard `start()` inside the try block.

### 2. Second GUIManager instance registered as listener
A new `GUIManager` instance is registered as a listener instead of reusing the one already stored in the field. The listener and the stored manager will be out of sync.

### 3. `writeLog` creates a new Logger on every call
A new `Logger` instance is constructed on every `writeLog` invocation. Store it as a class-level field instead.

### 4. Redundant null guard on `core`
There is a `core != null` guard that can never be false at that point in the startup sequence — it was already assigned or an exception would have been thrown.
