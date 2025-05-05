package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.Operations;
import me.hektortm.wosCore.database.DatabaseManager;
import me.hektortm.wosCore.database.IDAO;
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
            plugin.writeLog(logName, Level.SEVERE, "Failed to initialize unlockables tables: " + e);
        }
    }

    public void modifyUnlockable(UUID uuid, String id, Operations action) {
        switch (action) {
            case GIVE:
                try (Connection conn = db.getConnection();
                     PreparedStatement stmt = conn.prepareStatement("INSERT INTO playerdata_unlockables (uuid, id) VALUES (?, ?) ON CONFLICT(uuid, id) DO NOTHING")) {

                    stmt.setString(1, uuid.toString());
                    stmt.setString(2, id);
                    stmt.executeUpdate();
                } catch (SQLException e) {
                    plugin.writeLog(logName, Level.SEVERE, "Failed to give unlockable: " + e);
                }
                break;
            case TAKE:
                try (Connection conn = db.getConnection();
                     PreparedStatement stmt = conn.prepareStatement("DELETE FROM playerdata_unlockables WHERE uuid = ? AND id = ?")) {

                    stmt.setString(1, uuid.toString());
                    stmt.setString(2, id);
                    stmt.executeUpdate();
                } catch (SQLException e) {
                    plugin.writeLog(logName, Level.SEVERE, "Failed to take unlockable: " + e);
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
        }
    }

    public boolean getPlayerUnlockable(OfflinePlayer p, String id) {
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM player_unlockables WHERE uuid = ? AND id = ? AND temp = 0")) {
            stmt.setString(1, p.getUniqueId().toString());
            stmt.setString(2, id);
            ResultSet resultSet = stmt.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Failed to get Player Unlockable: " + e);
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
            return false;
        }
    }

}
