package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DatabaseManager;
import me.hektortm.woSSystems.systems.unlockables.utils.Action;
import org.bukkit.OfflinePlayer;

import java.sql.*;
import java.util.UUID;
import java.util.logging.Level;

public class UnlockableDAO {
    private Connection connection;
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);

    public UnlockableDAO(DatabaseManager databaseManager) {
        this.connection = databaseManager.getConnection();
        createTables();
    }

    private void createTables() {
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS unlockables (" +
                    "id TEXT PRIMARY KEY)");
            statement.execute("CREATE TABLE IF NOT EXISTS temp_unlockables (" +
                    "id TEXT PRIMARY KEY)");
            statement.execute("CREATE TABLE IF NOT EXISTS player_unlockables (" +
                    "uuid TEXT, " +
                    "id TEXT, " +
                    "PRIMARY KEY (uuid, id), " +
                    "FOREIGN KEY (uuid) REFERENCES playerdata(uuid), " +
                    "FOREIGN KEY (id) REFERENCES unlockables(id))");
            statement.execute("CREATE TABLE IF NOT EXISTS player_tempunlockables (" +
                    "uuid TEXT, " +
                    "id TEXT, " +
                    "PRIMARY KEY (uuid, id), " +
                    "FOREIGN KEY (uuid) REFERENCES playerdata(uuid), " +
                    "FOREIGN KEY (id) REFERENCES temp_unlockables(id))");
        } catch (SQLException e) {
            plugin.writeLog("UnlockableDAO", Level.SEVERE, "Could not create Tables: "+e.getMessage());
        }
    }

    public void addUnlockable(String id) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO unlockables (id) VALUES (?) ON CONFLICT(id) DO NOTHING")) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        }
    }

    public void deleteUnlockable(String id) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM unlockables WHERE id = ?")) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        }
    }

    public void addTempUnlockable(String id) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO temp_unlockables (id) VALUES (?) ON CONFLICT(id) DO NOTHING")) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        }
    }

    public void deleteTempUnlockable(String id) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM temp_unlockables WHERE id = ?")) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        }
    }

    public void modifyUnlockable(UUID uuid, String id, Action action) throws SQLException {
        switch (action) {
            case GIVE:
                try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO player_unlockables (uuid, id) VALUES (?, ?) ON CONFLICT(uuid, id) DO NOTHING")) {
                    stmt.setString(1, uuid.toString());
                    stmt.setString(2, id);
                    stmt.executeUpdate();
                }
                break;
            case TAKE:
                try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM player_unlockables WHERE uuid = ? AND id = ?")) {
                    stmt.setString(1, uuid.toString());
                    stmt.setString(2, id);
                    stmt.executeUpdate();
                }
                break;
        }
    }

    public void modifyTempUnlockable(UUID uuid, String id, Action action) throws SQLException {
        switch (action) {
            case GIVE:
                try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO player_tempunlockables (uuid, id) VALUES (?, ?) ON CONFLICT(uuid, id) DO NOTHING")) {
                    stmt.setString(1, uuid.toString());
                    stmt.setString(2, id);
                    stmt.executeUpdate();
                }
                break;
            case TAKE:
                try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM player_tempunlockables WHERE uuid = ? AND id = ?")) {
                    stmt.setString(1, uuid.toString());
                    stmt.setString(2, id);
                    stmt.executeUpdate();
                }
                break;
        }
    }

    public void removeAllTemps(UUID uuid) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM player_tempunlockables WHERE uuid = ?")) {
            stmt.setString(1, uuid.toString());
            stmt.executeUpdate();
        }
    }

    public boolean getPlayerUnlockable(OfflinePlayer p, String id) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT 1 FROM player_unlockables WHERE uuid = ? AND id = ?")) {
            stmt.setString(1, p.getUniqueId().toString());
            stmt.setString(2, id);
            ResultSet resultSet = stmt.executeQuery();
            return resultSet.next();
        }
    }
    public boolean getPlayerTempUnlockable(OfflinePlayer p, String id) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT 1 FROM player_tempunlockables WHERE uuid = ? AND id = ?")) {
            stmt.setString(1, p.getUniqueId().toString());
            stmt.setString(2, id);
            ResultSet resultSet = stmt.executeQuery();
            return resultSet.next();
        }
    }

}
