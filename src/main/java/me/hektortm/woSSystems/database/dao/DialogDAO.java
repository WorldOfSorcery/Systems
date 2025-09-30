package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.dataclasses.Interaction;
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

import java.sql.*;
import java.util.ArrayList;
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
                    "char_name_color VARCHAR(7)," +
                    "text_color VARCHAR(7)," +
                    "background_color VARCHAR(7)," +
                    "answer_background_color VARCHAR(7)," +
                    "fog_color VARCHAR(7)," +
                    "arrow_color VARCHAR(7)," +
                    "selected_color VARCHAR(7))");
            stmt.execute("CREATE TABLE IF NOT EXISTS dialog_pages(" +
                    "dialog_id VARCHAR(255), " +
                    "page_id INT, " +
                    "post_action VARCHAR(255)," +
                    "pre_action VARCHAR(255)," +
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
                    "answer_id INT," +
                    "answer_text VARCHAR(255)," +
                    "answer_reply TEXT," +
                    "answer_action VARCHAR(255)," +
                    "PRIMARY KEY (dialog_id))");
        }
    }

    private static boolean hasColumn(ResultSet rs, String col) {
        try {
            rs.findColumn(col); return true;
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE,
                    WoSSystems.getPlugin(WoSSystems.class),
                    "DID:a1d73b8e",
                    "Column " + col + " does not exist in ResultSet",
                    e
            ));
            return false;
        }
    }
    private static String getOrDefault(ResultSet rs, String col, String def) {
        try {
            return hasColumn(rs, col) && rs.getString(col) != null ? rs.getString(col) : def;
        } catch (SQLException e) {

            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE,
                    WoSSystems.getPlugin(WoSSystems.class),
                    "DID:cfb1f3e2",
                    "Failed to get column " + col + " from ResultSet, returning default value: " + def,
                    e
            ));
            return def;
        }
    }

    public void getDialog(String dialogId, CommandSender source, Player target) {
        String sql = "SELECT * FROM dialogs WHERE dialog_id = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, dialogId);
            ResultSet rs = pstmt.executeQuery();

            if (rs == null) {
                Utils.error(source, "dialogs", "error.notfound", "%id%", dialogId);
                return;
            }

            if (rs.next()) {
                String charName = plugin.getPlaceholderResolver().resolvePlaceholders( rs.getString("char_name"), target );
                String charNameColor        = getOrDefault(rs, "char_name_color", "#4f4a3e");
                String textColor            = getOrDefault(rs, "text_color", "#4f4a3e");
                String backgroundColor      = getOrDefault(rs, "background_color", "#f8ffe0");
                String answerBackgroundColor= getOrDefault(rs, "answer_background_color", "#f8ffe0");
                String fogColor             = getOrDefault(rs, "fog_color", "#000000");
                String arrowColor           = getOrDefault(rs, "arrow_color", "#cdff29");
                String selectedColor        = getOrDefault(rs, "selected_color", "#4f4a3e");

                List<Page> pages = getPages(dialogId, target);
                List<Answer> answers = getAnswers(dialogId);

                Dialogue.Builder dialogBuilder = new Dialogue.Builder().setDialogueID(dialogId)
                        .setDialogueText(textColor, 10)
                        .setCharacterNameText(charName, charNameColor, 20)
                        .setDialogueBackgroundImage("dialogue-background", backgroundColor, 0)
                        .setDialogueSpeed(1)

                        .setTypingSound("luxdialogues:luxdialogues.sounds.typing")
                        .setTypingSoundPitch(1.0)
                        .setTypingSoundVolume(1.0)
                        .setRange(10.0)
                        .setNameStartImage("name-start")
                        .setNameMidImage("name-mid")
                        .setNameEndImage("name-end")
                        .setFogImage("fog", fogColor)
                        .setNameImageColor("#ffffff")
                        .setArrowImage("hand", arrowColor, -7)
                        .setSelectionSound("luxdialogues:luxdialogues.sounds.selection")
                        .setAnswerBackgroundImage("answer-background", answerBackgroundColor, 140)
                        .setAnswerText(textColor, 13, selectedColor);

                for (Page page : pages) {
                    dialogBuilder.addPage(page);
                }
                if (answers != null) {
                    for (Answer answer : answers) {
                        dialogBuilder.addAnswer(answer);
                    }
                }

                plugin.getDialogueApi().sendDialogue(target, dialogBuilder.build());
                if (source instanceof Player) Utils.success(source, "dialogs", "info.triggered", "%dialog%", dialogId, "%player%", target.getName());
                else if (source instanceof ConsoleCommandSender) source.sendMessage("Dialog " + dialogId + " triggered for player " + target.getName());

            }

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

    public List<Page> getPages(String dialogId, Player target) {
        List<Page> pages = new ArrayList<>();
        String sql = "SELECT * FROM dialog_pages WHERE dialog_id = ? ORDER BY page_id ASC";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, dialogId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String preAction = rs.getString("pre_action");
                String postAction = rs.getString("post_action");
                Page.Builder pageBuilder = new Page.Builder();
                if (rs.getString("pre_action") != null) pageBuilder.addPreCallback(player -> plugin.getInteractionManager().triggerInteraction(preAction, player));
                if (rs.getString("post_action") != null) pageBuilder.addPostCallback(player -> plugin.getInteractionManager().triggerInteraction(postAction, player));

                for (String line : getPageLines(dialogId, rs.getInt("page_id"), target)) {
                    pageBuilder.addLine(line);
                }
                pages.add(pageBuilder.build());
            }
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE,
                    plugin,
                    "DID:65765d89",
                    "Failed to load dialog pages for dialog ID: " + dialogId,
                    e
            ));
        }

        return pages;

    }

    public List<Answer> getAnswers(String dialogId) {
        List<Answer> answers = new ArrayList<>();
        String sql = "SELECT * FROM dialog_answers WHERE dialog_id = ? ORDER BY answer_id ASC";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, dialogId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String action = rs.getString("answer_action");
                Answer.Builder answerBuilder = new Answer.Builder()
                        .setAnswerID(String.valueOf(rs.getInt("answer_id")))
                        .setAnswerText(rs.getString("answer_text"))
                        .addReplyMessage(rs.getString("answer_reply"))
                        .addCallback(player -> {
                            plugin.getInteractionManager().triggerInteraction(action, player);
                        });
                answers.add(answerBuilder.build());
            }
            return answers;
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE,
                    plugin,
                    "DID:da60aeb2",
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
                lines.add(plugin.getPlaceholderResolver().resolvePlaceholders( rs.getString("line_text"), target ) );
            }
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE,
                    plugin,
                    "DID:869cdc5b",
                    "Failed to load page lines for dialog ID: " + dialogId,
                    e
            ));
        }
        return lines;
    }



}
