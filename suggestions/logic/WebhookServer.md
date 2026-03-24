# WebhookServer

**Package:** `me.hektortm.woSSystems`

## Logic Issues

### 1. `webhookServer.start()` called outside the try-catch — NPE when constructor throws

In `WoSSystems.onEnable` (line 293–298):

```java
try {
    webhookServer = new WebhookServer(this, daoHub);
} catch (IOException e) {
    getLogger().severe("[Webhook] Failed to start: " + e.getMessage());
}
webhookServer.start();   // ← outside the try block
```

If the `WebhookServer` constructor throws `IOException`, the field remains `null` and the unconditional `webhookServer.start()` call on the next line throws `NullPointerException`, crashing the server. `webhookServer.start()` must be moved inside the `try` block (after the constructor), and a `return` or `disablePlugin` call should follow in the `catch`.

---

### 2. `handleInvalidate` dispatches world-state work asynchronously via a second `runTaskAsynchronously`

Lines 85–87:

```java
Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
        daoHub.handleWebhookInvalidation(type, id, editorUUID)
);
```

The webhook handler is already running on a thread-pool thread (the `HttpServer` executor). Submitting _another_ async task from that context is harmless for pure DB reads, but `handleWebhookInvalidation` calls `Bukkit.getPlayer(editorUUID)` (a Bukkit API call that must be made from the main thread). Accessing the player list from an async thread is not thread-safe and can return stale or corrupted results. The `Bukkit.getPlayer` call should be moved to a `runTask` (main-thread) callback, or the player lookup should be omitted and the UUID passed raw.

---

### 3. Secret compared with `equals` even when config value is `null`

Line 64:

```java
if (auth == null || !auth.equals(secret)) {
```

`secret` is populated from `plugin.getConfig().getString("webhook.secret")`, which returns `null` if the key is absent. When both `auth` and `secret` are `null`, the guard passes `auth == null` and rejects the request correctly — but if `secret` is `null` and `auth` is any non-null string, `auth.equals(null)` returns `false`, so the request is also rejected. That is correct *by accident*. The real bug is that a missing `webhook.secret` key in config silently makes the webhook reject every request with no startup warning. The constructor should validate that `secret` is non-null/non-blank and log a fatal warning if it is.
