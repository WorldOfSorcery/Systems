package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.Operations;
import me.hektortm.wosCore.database.DatabaseManager;
import me.hektortm.wosCore.database.IDAO;
import me.hektortm.wosCore.discord.DiscordLog;
import me.hektortm.wosCore.discord.DiscordLogger;
import org.bukkit.OfflinePlayer;

import java.sql.*;
import java.util.UUID;
import java.util.logging.Level;

public class UnlockableDAO implements IDAO {
    private final DatabaseManager db;
    private final DAOHub daoHub;
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final String logName = "UnlockableDAO";

    public UnlockableDAO(DatabaseManager db, DAOHub daoHub) {
        this.db = db;
        this.daoHub = daoHub;
    }

    @Override
    public void initializeTable() {
        try (Connection conn = db.getConnection(); Statement statement = conn.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS unlockables (" +
                    "id VARCHAR(255) PRIMARY KEY," +
                    "temp BOOLEAN)");
            statement.execute("CREATE TABLE IF NOT EXISTS playerdata_unlockables (" +
                    "uuid CHAR(36), " +
                    "id VARCHAR(255), " +
                    "temp BOOLEAN," +
                    "PRIMARY KEY (uuid, id))");
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "608ad2f4", "Failed to intialize Unlockable Tables: ", e
            ));
        }
    }

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

    public void modifyUnlockable(UUID uuid, String id, Operations action) {
        switch (action) {
            case GIVE:
                try (Connection conn = db.getConnection();


                    // Nothing on duplicate
                     PreparedStatement stmt = conn.prepareStatement("INSERT INTO playerdata_unlockables (uuid, id, temp) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE temp = ?")) {

                    stmt.setString(1, uuid.toString());
                    stmt.setString(2, id);
                    stmt.setBoolean(3, isTemp(id));
                    stmt.setBoolean(4, isTemp(id));
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
