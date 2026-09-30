package me.hektortm.woSSystems.database.dao;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.content.ApiSource;
import me.hektortm.woSSystems.content.ContentRegistry;
import me.hektortm.woSSystems.content.ContentStore;
import me.hektortm.woSSystems.content.Json;
import me.hektortm.woSSystems.utils.ActionHandler;
import me.hektortm.wosCore.Utils;
import me.hektortm.wosCore.api.WosApi;
import org.aselstudios.luxdialoguesapi.Builders.Answer;
import org.aselstudios.luxdialoguesapi.Builders.Dialogue;
import org.aselstudios.luxdialoguesapi.Builders.Page;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Builds LuxDialogues {@link Dialogue}s from the dialog definitions in wos-api
 * ({@code /v1/content/dialogs/{id}}).
 *
 * <p>The raw (unresolved) dialog is cached per id; player placeholders are
 * resolved at build time. Visual settings come from the dialog's {@code settings}
 * object as edited in the portal (character name, colors).</p>
 */
public class DialogDAO {
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final ContentStore<RawDialog> store;

    public DialogDAO(ContentRegistry registry, WosApi api, Logger log) {
        this.store = registry.register(new ContentStore<>("dialogs", "Dialog",
                ApiSource.tree(api, "/v1/content/dialogs", DialogDAO::map, log)));
    }

    // ── cached templates ──────────────────────────────────────────────────────

    private record RawAnswer(String id, String text, @Nullable String action) {}

    private record RawPage(@Nullable String preAction, @Nullable String postAction,
                           List<String> lineTemplates, List<RawAnswer> answers) {}

    private record RawDialog(String charNameTemplate, String charNameColor, String textColor,
                             String backgroundColor, String answerBackgroundColor, String fogColor,
                             String arrowColor, String selectedColor, List<RawPage> pages) {}

    private static RawDialog map(JsonObject tree) {
        JsonObject s = Json.object(tree, "settings");
        List<RawPage> pages = new ArrayList<>();
        for (JsonElement el : Json.array(tree, "pages")) {
            JsonObject p = el.getAsJsonObject();
            List<RawAnswer> answers = new ArrayList<>();
            for (JsonElement a : Json.array(p, "answers")) {
                JsonObject ans = a.getAsJsonObject();
                answers.add(new RawAnswer(Json.str(ans, "id"), Json.str(ans, "answer_text", ""), blankToNull(Json.str(ans, "action"))));
            }
            pages.add(new RawPage(blankToNull(Json.str(p, "pre_action")), blankToNull(Json.str(p, "post_action")),
                    Json.strings(p, "lines"), answers));
        }
        return new RawDialog(
                Json.str(s, "character_name", ""),
                Json.str(s, "character_name_color", "#4f4a3e"),
                Json.str(s, "text_color", "#4f4a3e"),
                Json.str(s, "background_image_color", "#f8ffe0"),
                Json.str(s, "answer_background_image_color", "#f8ffe0"),
                Json.str(s, "fog_color", "#000000"),
                Json.str(s, "arrow_image_color", "#cdff29"),
                Json.str(s, "answer_text_color", "#4f4a3e"),
                pages);
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }

    // ── build with per-player placeholder resolution ──────────────────────────

    /**
     * A fully built {@link Dialogue} for {@code target}, or {@code null} (with an
     * error to {@code source}) if the dialog id is unknown.
     */
    @Nullable
    public Dialogue buildDialog(String dialogId, @Nullable CommandSender source, Player target) {
        RawDialog raw = store.get(dialogId);
        if (raw == null) {
            Utils.error(source, "dialogs", "error.notfound", "%id%", dialogId);
            return null;
        }

        String charName = plugin.getPlaceholderResolver().resolvePlaceholders(raw.charNameTemplate(), target);

        Dialogue.Builder dialogBuilder = new Dialogue.Builder()
                .setDialogueID(dialogId)
                .setDialogueText(raw.textColor(), 10)
                .setCharacterNameText(charName, raw.charNameColor(), 20)
                .setDialogueBackgroundImage("dialogue-background", raw.backgroundColor(), 0)
                .setDialogueSpeed(1)
                .setTypingSound("luxdialogues:luxdialogues.sounds.typing", "master", 1.0, 1.0)
                .setRange(10.0)
                .setNameImage("name-start", "name-mid", "name-end", "#ffffff", 0)
                .setFogImage("fog", raw.fogColor())
                .setArrowImage("hand", raw.arrowColor(), -7)
                .setSelectionSound("luxdialogues:luxdialogues.sounds.selection", "master", 1.0, 1.0)
                .setAnswerBackgroundImage("answer-background", raw.answerBackgroundColor(), 140)
                .setAnswerText(raw.textColor(), 13, raw.selectedColor());

        for (RawPage rawPage : raw.pages()) {
            Page.Builder pageBuilder = new Page.Builder();
            if (rawPage.preAction() != null)
                pageBuilder.addPreCallback(p -> plugin.getInteractionManager().triggerInteraction(rawPage.preAction(), p, null));
            if (rawPage.postAction() != null)
                pageBuilder.addPostCallback(p -> plugin.getInteractionManager().triggerInteraction(rawPage.postAction(), p, null));

            for (String lineTemplate : rawPage.lineTemplates()) {
                pageBuilder.addLine(plugin.getPlaceholderResolver().resolvePlaceholders(lineTemplate, target));
            }
            for (RawAnswer a : rawPage.answers()) {
                List<String> actionList = a.action() == null ? List.of() : List.of(a.action());
                pageBuilder.addAnswer(new Answer.Builder()
                        .setAnswerID(a.id())
                        .setAnswerText(plugin.getPlaceholderResolver().resolvePlaceholders(a.text(), target))
                        .addCallback(p -> plugin.getActionHandler().executeActions(
                                p, actionList, ActionHandler.SourceType.DIALOG, dialogId, null))
                        .build());
            }
            dialogBuilder.addPage(pageBuilder.build());
        }

        if (source instanceof Player) Utils.success(source, "dialogs", "info.triggered", "%dialog%", dialogId, "%player%", target.getName());
        else if (source instanceof ConsoleCommandSender) source.sendMessage("Dialog " + dialogId + " triggered for player " + target.getName());

        return dialogBuilder.build();
    }
}
