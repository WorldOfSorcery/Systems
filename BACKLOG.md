# Backlog

## Large item preview in the chat `[item]` dialog

**Goal:** when a player clicks `[item]` in chat, the dialog should show the item much bigger than inventory size.

**Why it's not done yet:** Paper's item dialog body can't scale items. `width`/`height` only add empty space around the item; the client always draws it at 16×16. Scaling the item model in the resource pack doesn't work either, because dialogs use the same `gui` display context as inventories, so the item would get huge everywhere.

**Planned approach: font glyphs.**
- Resource pack: give each custom item a glyph in a custom font that points to a large copy of its texture. Glyph `height` controls the size on screen.
- Citems: add an optional `dialog_glyph` field (the glyph character) to the citem definition (wos-api content, AdminPortal editor, `Citem` model).
- `ChatManager.itemDialog`: if the shown item is a citem with a glyph, render the glyph as a centred plain-message body (in the custom font) with the name underneath. Otherwise fall back to the normal item body. Keep the hover tooltip available, e.g. via a small item body or a hover event on the glyph text.
- Could also be used for the profile showcase in `ProfileDialogs`.

## Quest triggers, waits and actions (portal prototype → plugin)

**Goal:** quests start on in-game events (talk to an NPC, finish a dialog, enter a region …), wait for events mid-quest, branch on dialog answers or on whichever event comes first, and act on other systems (give items, unlock unlockables, open GUIs …).

**State:** the AdminPortal Quest Builder already has the six nodes as a prototype (Trigger, Wait for, First of, Action, Dialog choice, Stage); the plugin doesn't run them. Two example quests are in the dev database (`proto_herbalist_errand`, `proto_mages_trial`).

**Spec:** [`docs/quest-builder-prototype.md`](docs/quest-builder-prototype.md) — node JSON, event and action catalogs with their hook points, engine design, open questions, build order. Fix the known engine issues in [`docs/quest-system.md`](docs/quest-system.md) first (dialog nodes never finish, unknown node types are skipped, delays don't survive restarts).
