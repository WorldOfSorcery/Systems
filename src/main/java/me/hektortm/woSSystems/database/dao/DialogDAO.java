package me.hektortm.woSSystems.database.dao;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.ActionHandler;
import me.hektortm.wosCore.Utils;
import me.hektortm.wosCore.database.DatabaseManager;
import me.hektortm.wosCore.database.IDAO;
import me.hektortm.wosCore.discord.DiscordLog;
import me.hektortm.wosCore.discord.DiscordLogger;
import org.aselstudios.luxdialoguesapi.Builders.Answer;
import org.aselstudios.luxdialoguesapi.Builders.Dialogue;
import org.aselstudios.luxdialoguesapi.Builders.Page;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class DialogDAO implements IDAO {
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final DatabaseManager db;
    private final DAOHub hub;

    public DialogDAO(DatabaseManager db, DAOHub hub) {
        this.db = db;
        this.hub = hub;
    }


    @Override
    public void initializeTable() throws SQLException {
        try (Connection conn = db.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS dialogs(" +
                    "dialog_id VARCHAR(255) PRIMARY KEY," +
                    "char_name VARCHAR(255)," +
                    "settings JSON)");
            stmt.execute("CREATE TABLE IF NOT EXISTS dialog_pages(" +
                    "dialog_id VARCHAR(255), " +
                    "page_id INT, " +
                    "settings JSON," +
                    "PRIMARY KEY (dialog_id, page_id), " +
                    "FOREIGN KEY (dialog_id) REFERENCES dialogs(dialog_id) ON DELETE CASCADE)");
            stmt.execute("CREATE TABLE IF NOT EXISTS page_lines(" +
                    "dialog_id VARCHAR(255), " +
                    "page_id INT, " +
                    "line_id INT, " +
                    "line_text VARCHAR(255)," +
                    "PRIMARY KEY (dialog_id, page_id, line_id), " +
                    "FOREIGN KEY (dialog_id, page_id) REFERENCES dialog_pages(dialog_id, page_id) ON DELETE CASCADE)");
            stmt.execute("CREATE TABLE IF NOT EXISTS dialog_answers(" +
                    "dialog_id VARCHAR(255), " +
                    "page_id INT, " +
                    "answer_id INT," +
                    "answer_text VARCHAR(255)," +
                    "settings JSON," +
                    "PRIMARY KEY (dialog_id, page_id, answer_id)," +
                    "FOREIGN KEY (dialog_id, page_id) REFERENCES dialog_pages(dialog_id, page_id) ON DELETE CASCADE)");
        }
    }

    public void getDialog(String dialogId, @Nullable CommandSender source, Player target) {
        String sql = "SELECT * FROM dialogs WHERE dialog_id = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, dialogId);
            ResultSet rs = pstmt.executeQuery();

            if (!rs.next()) {
                Utils.error(source, "dialogs", "error.notfound", "%id%", dialogId);
                return;
            }

            String charName = plugin.getPlaceholderResolver().resolvePlaceholders(rs.getString("char_name"), target);
            String settingsRaw = rs.getString("settings");
            JsonObject s = JsonParser.parseString(settingsRaw != null ? settingsRaw : "{}").getAsJsonObject();

            List<Page> pages = getPages(dialogId, target);

            Dialogue.Builder dialogBuilder = buildDialogue(dialogId, charName, s);
            for (Page page : pages) dialogBuilder.addPage(page);

            plugin.getDialogueApi().sendDialogue(target, dialogBuilder.build(), "1");
            if (source instanceof Player) Utils.success(source, "dialogs", "info.triggered", "%dialog%", dialogId, "%player%", target.getName());
            else if (source instanceof ConsoleCommandSender) source.sendMessage("Dialog " + dialogId + " triggered for player " + target.getName());

        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE,
                    plugin,
                    "DID:8e45baa3",
                    "Failed to load dialog for dialog ID: " + dialogId,
                    e
            ));
        }
    }

    private Dialogue.Builder buildDialogue(String dialogId, String charName, JsonObject s) {
        Dialogue.Builder b = new Dialogue.Builder().setDialogueID(dialogId);

        b.setRange(getDouble(s, "range", 10.0));
        b.setCharacterNameText(charName, getString(s, "characterNameTextColor", "#4f4a3e"), getInt(s, "characterNameTextOffset", 20));
        b.setDialogueText(getString(s, "dialogueTextColor", "#4f4a3e"), getInt(s, "dialogueTextOffset", 10));
        b.setDialogueBackgroundImage(getString(s, "dialogueBackgroundImage", "dialogue-background"), getString(s, "dialogueBackgroundImageColor", "#f8ffe0"), getInt(s, "dialogueBackgroundImageOffset", 0));
        b.setAnswerText(getString(s, "answerTextColor", "#4f4a3e"), getInt(s, "answerTextOffset", 13), getString(s, "answerSelectedTextColor", "#4f4a3e"));
        b.setAnswerBackgroundImage(getString(s, "answerBackgroundImage", "answer-background"), getString(s, "answerBackgroundImageColor", "#f8ffe0"), getInt(s, "answerBackgroundImageOffset", 140));
        b.setNameImage(getString(s, "nameStartImage", "name-start"), getString(s, "nameMidImage", "name-mid"), getString(s, "nameEndImage", "name-end"), getString(s, "nameImageColor", "#ffffff"), getInt(s, "nameBackgroundImageOffset", 0));
        b.setArrowImage(getString(s, "arrowImage", "hand"), getString(s, "arrowImageColor", "#cdff29"), getInt(s, "arrowImageOffset", -7));
        b.setFogImage(getString(s, "fogImage", "fog"), getString(s, "fogColor", "#000000"));
        b.setDialogueSpeed(getInt(s, "dialogueSpeed", 1));
        b.setTypingSound(getString(s, "typingSound", "luxdialogues:luxdialogues.sounds.typing"), getString(s, "typingSoundSource", "master"), getDouble(s, "typingSoundVolume", 1.0), getDouble(s, "typingSoundPitch", 1.0));
        b.setSelectionSound(getString(s, "selectionSound", "luxdialogues:luxdialogues.sounds.selection"), getString(s, "selectionSoundSource", "master"), getDouble(s, "selectionSoundVolume", 1.0), getDouble(s, "selectionSoundPitch", 1.0));

        if (s.has("effect"))          b.setEffect(s.get("effect").getAsString());
        if (s.has("preventExit"))     b.setPreventExit(s.get("preventExit").getAsBoolean());
        if (s.has("preventSkip"))     b.setPreventSkip(s.get("preventSkip").getAsBoolean());
        if (s.has("answerNumbers"))   b.setAnswerNumbers(s.get("answerNumbers").getAsBoolean());
        if (s.has("characterImage"))  b.setCharacterImage(getString(s, "characterImage", null), getString(s, "characterImageColor", "#ffffff"), getInt(s, "characterImageOffset", 0));

        return b;
    }

    // --- JSON helpers ---

    private static String getString(JsonObject s, String key, String def) {
        return s.has(key) && !s.get(key).isJsonNull() ? s.get(key).getAsString() : def;
    }

    private static int getInt(JsonObject s, String key, int def) {
        return s.has(key) && !s.get(key).isJsonNull() ? s.get(key).getAsInt() : def;
    }

    private static double getDouble(JsonObject s, String key, double def) {
        return s.has(key) && !s.get(key).isJsonNull() ? s.get(key).getAsDouble() : def;
    }

    private static List<String> getJsonStringList(JsonObject s, String key) {
        if (!s.has(key) || s.get(key).isJsonNull()) return new ArrayList<>();
        List<String> result = new ArrayList<>();
        JsonArray arr = s.get(key).getAsJsonArray();
        for (int i = 0; i < arr.size(); i++) result.add(arr.get(i).getAsString());
        return result;
    }

    // --- Page / line / answer loading ---



    public List<Page> getPages(String dialogId, Player target) {
        List<Page> pages = new ArrayList<>();
        String sql = "SELECT * FROM dialog_pages WHERE dialog_id = ? ORDER BY page_id ASC";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, dialogId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int pageId = rs.getInt("page_id");
                String settingsRaw = rs.getString("settings");
                JsonObject s = JsonParser.parseString(settingsRaw != null ? settingsRaw : "{}").getAsJsonObject();

                Page.Builder pageBuilder = new Page.Builder();

                for (String action : getJsonStringList(s, "preActions")) {
                    pageBuilder.addPreCallback(player -> plugin.getInteractionManager().triggerInteraction(action, player, null));
                }
                for (String action : getJsonStringList(s, "postActions")) {
                    pageBuilder.addPostCallback(player -> plugin.getInteractionManager().triggerInteraction(action, player, null));
                }
                for (String action : getJsonStringList(s, "exitActions")) {
                    pageBuilder.addExitCallback(player -> plugin.getInteractionManager().triggerInteraction(action, player, null));
                }

                for (String line : getPageLines(dialogId, pageId, target)) {
                    pageBuilder.addLine(line);
                }

                // goto disables answers — the target page ID is stored but handled by the API
                boolean hasGoto = s.has("goto") && !s.get("goto").isJsonNull();
                if (hasGoto) {
                    pageBuilder.setGoTo(Collections.singletonList(s.get("goto").getAsString()));
                } else {
                    for (Answer answer : getAnswers(dialogId, pageId)) {
                        pageBuilder.addAnswer(answer);
                    }
                }

                pages.add(pageBuilder.build());
            }
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE,
                    plugin,
                    "65765d89",
                    "Failed to load dialog pages for dialog ID: " + dialogId,
                    e
            ));
        }
        return pages;
    }

    public List<Answer> getAnswers(String dialogId, int page_id) {
        List<Answer> answers = new ArrayList<>();
        String sql = "SELECT * FROM dialog_answers WHERE dialog_id = ? AND page_id = ? ORDER BY answer_id ASC";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, dialogId);
            pstmt.setInt(2, page_id);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String settingsRaw = rs.getString("settings");
                JsonObject s = JsonParser.parseString(settingsRaw != null ? settingsRaw : "{}").getAsJsonObject();

                List<String> actions = getJsonStringList(s, "actions");

                Answer.Builder answerBuilder = new Answer.Builder()
                        .setAnswerID(String.valueOf(rs.getInt("answer_id")))
                        .setAnswerText(rs.getString("answer_text"));

                if (!actions.isEmpty()) {
                    answerBuilder.addCallback(player ->
                            plugin.getActionHandler().executeActions(player, actions, ActionHandler.SourceType.DIALOG, dialogId, null));
                }

                String gotoPage = getString(s, "goto", "");
                if (!gotoPage.isEmpty()) {
                    answerBuilder.setGoTo(Collections.singletonList(gotoPage));
                }

                for (String reply : getJsonStringList(s, "reply")) {
                    answerBuilder.addReplyMessage(reply);
                }

                if (s.has("sound") && !s.get("sound").isJsonNull()) {
                    JsonObject sound = s.get("sound").getAsJsonObject();
                    answerBuilder.setSound(
                            getString(sound, "id", ""),
                            getString(sound, "source", "master"),
                            getDouble(sound, "volume", 1.0),
                            getDouble(sound, "pitch", 1.0));
                }

                answers.add(answerBuilder.build());
            }
            return answers;
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE,
                    plugin,
                    "da60aeb2",
                    "Failed to load dialog answers for dialog ID: " + dialogId,
                    e
            ));
            return answers;
        }
    }

    public List<String> getPageLines(String dialogId, int PageId, Player target) {
        String sql = "SELECT line_text FROM page_lines WHERE dialog_id = ? AND page_id = ? ORDER BY line_id ASC";
        List<String> lines = new ArrayList<>();
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, dialogId);
            pstmt.setInt(2, PageId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                lines.add(plugin.getPlaceholderResolver().resolvePlaceholders(rs.getString("line_text"), target));
            }
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE,
                    plugin,
                    "869cdc5b",
                    "Failed to load page lines for dialog ID: " + dialogId,
                    e
            ));
        }
        return lines;
    }
}
