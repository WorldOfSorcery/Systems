package me.hektortm.woSSystems.systems.bugs;

import com.google.gson.JsonElement;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.wosCore.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * /bug: files a bug report from the game on a dialog screen: a title, what
 * happened and how to make it happen again in the player's own words, plus
 * how bad and how often (dropdowns). Players aren't asked which system it's
 * in: they can't know, staff sort that in the portal. Send checks the
 * answers: anything missing reopens the dialog with the problems listed and
 * the answers kept. The player's location is added, and the report goes to
 * wos-api, which lists it in the portal and posts it to Discord. Texts: lang
 * file "bugs".
 */
public final class BugCommand implements CommandExecutor {

    private static final long COOLDOWN_MS = 60 * 1000L;
    private static final String NONE = "none"; // the "pick one" entry of a dropdown

    private static final ClickCallback.Options ONCE = ClickCallback.Options.builder().uses(1).build();

    private final WoSSystems plugin;
    private final Map<UUID, Long> lastSent = new ConcurrentHashMap<>();

    public BugCommand(WoSSystems plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can report bugs in game (staff: use the portal).");
            return true;
        }
        if (onCooldown(player)) {
            msg(player, "cooldown");
            return true;
        }
        player.showDialog(dialog(player, new BugReportForm(), List.of()));
        return true;
    }

    private boolean onCooldown(Player player) {
        Long last = lastSent.get(player.getUniqueId());
        return last != null && System.currentTimeMillis() - last < COOLDOWN_MS;
    }

    // ── the dialog ──────────────────────────────────────────────────────────────

    /** The form, filled with {@code form}'s answers and, after a failed send, what's missing. */
    private Dialog dialog(Player player, BugReportForm form, List<String> problems) {
        List<DialogBody> body = new ArrayList<>();
        body.add(DialogBody.plainMessage(text(lang("dialog.intro")), 300));
        if (!problems.isEmpty()) {
            body.add(DialogBody.plainMessage(text(lang("dialog.problems").replace("%problems%", String.join(", ", problems))), 300));
        }
        List<DialogInput> inputs = List.of(
                textField("title", "field.title", form.title, BugReportForm.MAX_TITLE, false),
                textField("what", "field.what", form.what, BugReportForm.MAX_TEXT, true),
                textField("steps", "field.steps", form.steps, BugReportForm.MAX_TEXT, true),
                dropdown("severity", "field.severity", List.copyOf(BugReportForm.SEVERITIES.keySet()),
                        List.copyOf(BugReportForm.SEVERITIES.values()), form.severity),
                dropdown("frequency", "field.frequency", List.copyOf(BugReportForm.FREQUENCIES.keySet()),
                        List.copyOf(BugReportForm.FREQUENCIES.values()), form.frequency));

        ActionButton send = ActionButton.builder(text(lang("button.send")))
                .action(DialogAction.customClick((response, audience) -> submit(player, response), ONCE))
                .build();
        ActionButton cancel = ActionButton.builder(text(lang("button.cancel"))).build(); // no action: closes

        return Dialog.create(factory -> factory.empty()
                .base(DialogBase.builder(text(lang("dialog.title")))
                        .canCloseWithEscape(true)
                        .body(body)
                        .inputs(inputs)
                        .build())
                .type(DialogType.confirmation(send, cancel)));
    }

    /** A dropdown whose first entry is "pick one" (id {@link #NONE}); {@code current} is preselected. */
    private SingleOptionDialogInput dropdown(String key, String labelKey, List<String> ids, List<String> labels, String current) {
        List<SingleOptionDialogInput.OptionEntry> entries = new ArrayList<>();
        entries.add(SingleOptionDialogInput.OptionEntry.create(NONE, text(lang("option.pick")), !ids.contains(current)));
        for (int i = 0; i < ids.size(); i++) {
            entries.add(SingleOptionDialogInput.OptionEntry.create(optionId(ids.get(i)), Component.text(labels.get(i)), ids.get(i).equals(current)));
        }
        return DialogInput.singleOption(key, text(lang(labelKey)), entries).width(300).build();
    }

    private TextDialogInput textField(String key, String labelKey, String current, int maxLength, boolean multiline) {
        TextDialogInput.Builder b = DialogInput.text(key, text(lang(labelKey))).initial(current).maxLength(maxLength).width(300);
        if (multiline) b.multiline(TextDialogInput.MultilineOptions.create(4, null));
        return b.build();
    }

    /** Option ids must be simple: "GUIs" → "guis", "Time & events" → "time_events". */
    static String optionId(String value) {
        return value.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9]+", "_").replaceAll("^_|_$", "");
    }

    /** The option's value for a picked id ("" for "pick one" or an unknown id). */
    static String fromOptionId(List<String> values, String id) {
        if (id == null) return "";
        for (String v : values) if (optionId(v).equals(id)) return v;
        return "";
    }

    // ── sending ─────────────────────────────────────────────────────────────────

    private void submit(Player player, DialogResponseView response) {
        BugReportForm form = new BugReportForm();
        form.title = orEmpty(response.getText("title"));
        form.what = orEmpty(response.getText("what"));
        form.steps = orEmpty(response.getText("steps"));
        form.severity = fromOptionId(List.copyOf(BugReportForm.SEVERITIES.keySet()), response.getText("severity"));
        form.frequency = fromOptionId(List.copyOf(BugReportForm.FREQUENCIES.keySet()), response.getText("frequency"));

        List<String> problems = form.problems();
        if (!problems.isEmpty()) {
            player.showDialog(dialog(player, form, problems)); // again, answers kept
            return;
        }
        if (onCooldown(player)) {
            msg(player, "cooldown");
            return;
        }
        lastSent.put(player.getUniqueId(), System.currentTimeMillis());

        Map<String, Object> body = form.body(location(player.getLocation()));
        body.put("player_uuid", player.getUniqueId().toString());
        body.put("player_name", player.getName());
        msg(player, "sending");
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String id;
            try {
                JsonElement created = plugin.getCore().getApi().send("POST", "/v1/server/bugs", body);
                id = created.getAsJsonObject().get("id").getAsString();
            } catch (Exception e) {
                plugin.getLogger().warning("[Bugs] report from " + player.getName() + " not sent: " + e.getMessage());
                lastSent.remove(player.getUniqueId()); // let them try again right away
                Bukkit.getScheduler().runTask(plugin, () -> msg(player, "failed"));
                return;
            }
            String sentId = id;
            Bukkit.getScheduler().runTask(plugin, () -> player.sendMessage(text(lang("sent").replace("%id%", sentId))));
        });
    }

    static String location(Location l) {
        return l.getWorld() == null ? "" : String.format("%s %d %d %d", l.getWorld().getName(), l.getBlockX(), l.getBlockY(), l.getBlockZ());
    }

    // ── texts ───────────────────────────────────────────────────────────────────

    private void msg(Player player, String key) {
        player.sendMessage(text(lang(key)));
    }

    private String lang(String key) {
        return plugin.getLangManager().getMessage("bugs", key);
    }

    private static Component text(String message) {
        return LegacyComponentSerializer.legacySection().deserialize(Utils.parseColorCodeString(message));
    }

    private static String orEmpty(String s) {
        return s == null ? "" : s;
    }
}
