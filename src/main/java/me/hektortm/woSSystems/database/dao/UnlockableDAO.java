package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.SchemaManager;
import me.hektortm.woSSystems.utils.Operations;
import me.hektortm.woSSystems.utils.model.Unlockable;
import me.hektortm.wosCore.database.DatabaseManager;
import me.hektortm.wosCore.database.IDAO;
import me.hektortm.wosCore.discord.DiscordLog;
import me.hektortm.wosCore.discord.DiscordLogger;
import org.bukkit.OfflinePlayer;

import java.sql.*;
import java.util.UUID;
import java.util.logging.Level;

/**
 * DAO for unlockable content definitions and per-player unlock state.
 *
 * <p>Unlockables can be permanent or temporary ({@code temp = 1}).  Temporary
 * unlockables are automatically removed on server startup via
 * {@link #resetDailyUnlockables()}, and also on player quit via
 * {@link #removeAllTemps(UUID)}.</p>
 *
 * <p>Tables managed: {@code unlockables} (definitions, via
 * {@link me.hektortm.woSSystems.database.SchemaManager}),
 * {@code playerdata_unlockables} (per-player state, manual).</p>
 */
public class UnlockableDAO implements IDAO {
    private final DatabaseManager db;
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final String logName = "UnlockableDAO";

    public UnlockableDAO(DatabaseManager db) { this.db = db; }

    @Override
    public void initializeTable() {
        SchemaManager.syncTable(db, Unlockable.class);

        // Player unlockables table is relational — keep manual
        try (Connection conn = db.getConnection(); Statement statement = conn.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS playerdata_unlockables (" +
                    "uuid CHAR(36), " +
                    "id VARCHAR(255), " +
                    "temp BOOLEAN," +
                    "PRIMARY KEY (uuid, id))");
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "608ad2f4", "Failed to initialize Unlockable Tables: ", e
            ));
        }
    }

    /**
     * Deletes all player unlockable rows whose ID starts with {@code "daily_"}.
     * Intended to be called at server startup or midnight reset.
     */
    public void resetDailyUnlockables() {
        String sql = "DELETE FROM playerdata_unlockables WHERE id LIKE 'daily_%'";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "UnlockableDAO:reset", "Failed to reset daily unlockables: ", e
            ));
        }
    }

    /**
     * Checks whether an unlockable definition with the given ID exists.
     *
     * @param id the unlockable ID
     * @return {@code true} if the definition exists
     */
    public boolean unlockableExists(String id) {
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM unlockables WHERE id = ?")) {
            stmt.setString(1, id);
            ResultSet resultSet = stmt.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "b1f4958c", "Failed to check if Unlockable exists: "
                    + "\n ID: "+id, e
            ));
            return false;
        }
    }

    /**
     * Returns whether the given unlockable is marked as temporary in its
     * definition.  Temporary unlockables are cleaned up automatically.
     *
     * @param id the unlockable ID
     * @return {@code true} if the unlockable is temporary
     */
    public boolean isTemp(String id) {
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT temp FROM unlockables WHERE id = ?")) {
            stmt.setString(1, id);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return resultSet.getBoolean("temp");
            }
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "0188787c", "Failed to get Unlockable state: "
                    + "\n ID: "+id, e
            ));
        }
        return false;
    }

    /**
     * Grants or revokes an unlockable for a player based on the given operation.
     * <ul>
     *   <li>{@link Operations#GIVE} — inserts the unlockable with the correct
     *       temp flag; does nothing on duplicate.</li>
     *   <li>{@link Operations#TAKE} — deletes the player's unlockable row.</li>
     * </ul>
     *
     * @param uuid   the player's UUID
     * @param id     the unlockable ID
     * @param action {@code GIVE} or {@code TAKE}
     */
    public void modifyUnlockable(UUID uuid, String id, Operations action) {
        switch (action) {
            case GIVE:
                try (Connection conn = db.getConnection();


                    // Nothing on duplicate
                     PreparedStatement stmt = conn.prepareStatement("INSERT INTO playerdata_unlockables (uuid, id, temp) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE temp = ?")) {

                    boolean temp = isTemp(id);
                    stmt.setString(1, uuid.toString());
                    stmt.setString(2, id);
                    stmt.setBoolean(3, temp);
                    stmt.setBoolean(4, temp);
                    stmt.executeUpdate();
                } catch (SQLException e) {
                    plugin.writeLog(logName, Level.SEVERE, "Failed to give unlockable: " + e);
                    DiscordLogger.log(new DiscordLog(
                            Level.SEVERE, plugin, "97e418ac", "Failed to give Unlockable: "
                            + "\n UUID: "+uuid
                            + "\n ID: "+id
                            + "\n Operation: "+action, e
                    ));
                }
                break;
            case TAKE:
                try (Connection conn = db.getConnection();
                     PreparedStatement stmt = conn.prepareStatement("DELETE FROM playerdata_unlockables WHERE uuid = ? AND id = ?")) {

                    stmt.setString(1, uuid.toString());
                    stmt.setString(2, id);
                    stmt.executeUpdate();
                } catch (SQLException e) {
                    DiscordLogger.log(new DiscordLog(
                            Level.SEVERE, plugin, "5f14b691", "Failed to take Unlockable: "
                            + "\n UUID: "+uuid
                            + "\n ID: "+id
                            + "\n Operation: "+action, e
                    ));
                }
                break;
        }
    }

    /**
     * Removes all temporary unlockables for the given player.
     * Typically called on player quit.
     *
     * @param uuid the player's UUID
     */
    public void removeAllTemps(UUID uuid) {
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM playerdata_unlockables WHERE uuid = ? AND temp = 1")) {
            stmt.setString(1, uuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Failed to remove all temp unlockables: " + e);
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "ddc0a9ef", "Failed to remove all temp Unlockable: "
                    + "\n UUID: "+uuid, e
            ));
        }
    }

    /**
     * Returns {@code true} if the player has a <em>permanent</em> ({@code temp = 0})
     * copy of the given unlockable.
     *
     * @param p  the player to check
     * @param id the unlockable ID
     * @return {@code true} if the permanent unlockable is held
     */
    public boolean getPlayerUnlockable(OfflinePlayer p, String id) {
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM playerdata_unlockables WHERE uuid = ? AND id = ? AND temp = 0")) {
            stmt.setString(1, p.getUniqueId().toString());
            stmt.setString(2, id);
            ResultSet resultSet = stmt.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Failed to get Player unlockables: " + e);
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "34e85b61", "Failed to remove all temp Unlockable: "
                    + "\n Offline Player: "+p
                    + "\n UUID: "+p.getUniqueId()
                    + "\n ID: "+id, e
            ));
            return false;
        }
    }
    /**
     * Returns {@code true} if the player has a <em>temporary</em> ({@code temp = 1})
     * copy of the given unlockable.
     *
     * @param p  the player to check
     * @param id the unlockable ID
     * @return {@code true} if the temporary unlockable is held
     */
    public boolean getPlayerTempUnlockable(OfflinePlayer p, String id) {
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM playerdata_unlockables WHERE uuid = ? AND id = ? AND temp = 1")) {
            stmt.setString(1, p.getUniqueId().toString());
            stmt.setString(2, id);
            ResultSet resultSet = stmt.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Failed to get Player TempUnlockable: " + e);
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "d23abf7a", "Failed to get Player temp Unlockable: "
                    + "\n Offline Player: "+p
                    + "\n UUID: "+p.getUniqueId()
                    + "\n ID: "+id, e
            ));
            return false;
        }
    }

}
