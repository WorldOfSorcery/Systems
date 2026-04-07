package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.WoSSystems;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * DAO for building and delivering LuxDialogues {@link Dialogue} instances to players.
 *
 * <p>Raw dialog data (unresolved templates) is loaded from the database once and
 * stored in {@link #cache} per {@code dialog_id}. Player-specific placeholder
 * resolution happens at build time, so the cache key is always just the dialog ID.</p>
 *
 * <p>Tables managed: {@code dialogs}, {@code dialog_pages}, {@code page_lines},
 * {@code dialog_answers}.</p>
 */
public class DialogDAO implements IDAO {
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final DatabaseManager db;

    /** Raw (unresolved) dialog templates keyed by dialog_id. */
    private final Map<String, RawDialog> cache = new ConcurrentHashMap<>();

    public DialogDAO(DatabaseManager db) { this.db = db; }

    // -------------------------------------------------------------------------
    // Inner records — unresolved templates stored in the cache
    // -------------------------------------------------------------------------

    private record RawAnswer(int id, String text, String action) {}

    private record RawPage(
            @Nullable String preAction,
            @Nullable String postAction,
            List<String> lineTemplates,
            List<RawAnswer> answers
    ) {}

    private record RawDialog(
            String charNameTemplate,
            String charNameColor,
            String textColor,
            String backgroundColor,
            String answerBackgroundColor,
            String fogColor,
            String arrowColor,
            String selectedColor,
            List<RawPage> pages
    ) {}

    // -------------------------------------------------------------------------
    // Schema
    // -------------------------------------------------------------------------

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
                    "page_id INT, " +
                    "answer_id INT," +
                    "answer_text VARCHAR(255)," +
                    "answer_reply TEXT," +
                    "answer_action VARCHAR(255)," +
                    "PRIMARY KEY (dialog_id))");
        }
    }

    // -------------------------------------------------------------------------
    // Cache management
    // -------------------------------------------------------------------------

    /**
     * Preloads all dialogs from the database into the cache.
     * Call this on startup after {@link #initializeTable()}.
     */
    public void preloadAll() {
        String sql = "SELECT dialog FROM dialogs";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            int count = 0;
            while (rs.next()) {
                String id = rs.getString("dialog_id");
                RawDialog raw = loadRawFromDb(id);
                if (raw != null) {
                    cache.put(id, raw);
                    count++;
                }
            }
            plugin.getLogger().info("DialogDAO: preloaded " + count + " dialog(s) into cache.");
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, "DialogDAO:preload", "Failed to preload dialogs: ", e);
        }
    }

    /** Removes a single entry from the cache, forcing a DB reload on next use. */
    public void invalidate(String dialogId) {
        cache.remove(dialogId);
    }

    /** Clears the entire cache. */
    public void invalidateAll() {
        cache.clear();
    }

    // -------------------------------------------------------------------------
    // Raw DB loading (no placeholder resolution — one connection for all queries)
    // -------------------------------------------------------------------------

    @Nullable
    private RawDialog loadRawFromDb(String dialogId) {
        String sql = "SELECT * FROM dialogs WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, dialogId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) return null;

                return new RawDialog(
                        rs.getString("char_name"),
                        getOrDefault(rs, "char_name_color",         "#4f4a3e"),
                        getOrDefault(rs, "text_color",              "#4f4a3e"),
                        getOrDefault(rs, "background_color",        "#f8ffe0"),
                        getOrDefault(rs, "answer_background_color", "#f8ffe0"),
                        getOrDefault(rs, "fog_color",               "#000000"),
                        getOrDefault(rs, "arrow_color",             "#cdff29"),
                        getOrDefault(rs, "selected_color",          "#4f4a3e"),
                        loadRawPages(conn, dialogId)
                );
            }
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(Level.SEVERE, plugin, "DID:8e45baa3",
                    "Failed to load raw dialog for ID: " + dialogId, e));
            return null;
        }
    }

    private List<RawPage> loadRawPages(Connection conn, String dialogId) throws SQLException {
        List<RawPage> pages = new ArrayList<>();
        String sql = "SELECT * FROM dialog_pages WHERE dialog_id = ? ORDER BY page_id ASC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, dialogId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int pageId = rs.getInt("page_id");
                    pages.add(new RawPage(
                            rs.getString("pre_action"),
                            rs.getString("post_action"),
                            loadRawLines(conn, dialogId, pageId),
                            loadRawAnswers(conn, dialogId, pageId)
                    ));
                }
            }
        }
        return pages;
    }

    private List<String> loadRawLines(Connection conn, String dialogId, int pageId) throws SQLException {
        List<String> lines = new ArrayList<>();
        String sql = "SELECT line_text FROM page_lines WHERE dialog_id = ? AND page_id = ? ORDER BY line_id ASC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, dialogId);
            pstmt.setInt(2, pageId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) lines.add(rs.getString("line_text"));
            }
        }
        return lines;
    }

    private List<RawAnswer> loadRawAnswers(Connection conn, String dialogId, int pageId) throws SQLException {
        List<RawAnswer> answers = new ArrayList<>();
        String sql = "SELECT * FROM dialog_answers WHERE dialog_id = ? AND page_id = ? ORDER BY answer_id ASC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, dialogId);
            pstmt.setInt(2, pageId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    answers.add(new RawAnswer(
                            rs.getInt("answer_id"),
                            rs.getString("answer_text"),
                            rs.getString("answer_action")
                    ));
                }
            }
        }
        return answers;
    }

    // -------------------------------------------------------------------------
    // Public API — build with per-player placeholder resolution
    // -------------------------------------------------------------------------

    /**
     * Returns a fully-built {@link Dialogue} for {@code target}, resolving all
     * placeholder templates from the cached raw data.  Falls back to a DB load
     * if the dialog is not yet cached.
     *
     * @param dialogId the dialog ID to look up
     * @param source   the command sender who triggered the dialog (used for feedback); may be {@code null}
     * @param target   the player who will receive the dialog
     * @return the assembled {@link Dialogue}, or {@code null} if the ID is unknown
     */
    @Nullable
    public Dialogue buildDialog(String dialogId, @Nullable CommandSender source, Player target) {
        RawDialog raw = cache.computeIfAbsent(dialogId, this::loadRawFromDb);
        if (raw == null) {
            Utils.error(source, "dialogs", "error.notfound", "%id%", dialogId);
            return null;
        }

        // Resolve player-specific placeholders at render time — not stored back into the cache
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
                List<String> actionList = List.of(a.action());
                pageBuilder.addAnswer(new Answer.Builder()
                        .setAnswerID(String.valueOf(a.id()))
                        .setAnswerText(a.text())
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

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static boolean hasColumn(ResultSet rs, String col) {
        try {
            rs.findColumn(col);
            return true;
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
                    "cfb1f3e2",
                    "Failed to get column " + col + " from ResultSet, returning default value: " + def,
                    e
            ));
            return def;
        }
    }
}
