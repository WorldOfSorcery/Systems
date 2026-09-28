# debug

**Package:** `me.hektortm.woSSystems`

## Suggestions

### 1. Debug command is registered and ships in production
The `debug` command is registered in `registerCommands` and will be accessible on any server. It executes a hardcoded `getDialog("test", ...)` call — a leftover from development. Either remove it entirely or gate it behind an environment check so it is never reachable on a production server.
