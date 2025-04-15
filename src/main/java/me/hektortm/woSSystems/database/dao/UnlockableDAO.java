package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.systems.unlockables.utils.Action;
import me.hektortm.wosCore.database.DatabaseManager;
import me.hektortm.wosCore.database.IDAO;
import org.bukkit.OfflinePlayer;

import java.sql.*;
import java.util.UUID;
import java.util.logging.Level;

public class UnlockableDAO implements IDAO {
    private final Connection conn;
    private final DatabaseManager db;
    private final DAOHub daoHub;
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);

    public UnlockableDAO(DatabaseManager db, DAOHub daoHub) {
        this.db = db;
        this.daoHub = daoHub;
        this.conn = db.getConnection();
    }

    @Override
    public void initializeTable() throws SQLException {
        try (Statement statement = conn.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS unlockables (" +
                    "id TEXT PRIMARY KEY," +
                    "temp BOOLEAN)");
            statement.execute("CREATE TABLE IF NOT EXISTS playerdata_unlockables (" +
                    "uuid TEXT, " +
                    "id TEXT, " +
                    "temp BOOLEAN," +
                    "PRIMARY KEY (uuid, id), " +
                    "FOREIGN KEY (uuid) REFERENCES playerdata(uuid), " +
                    "FOREIGN KEY (id) REFERENCES unlockables(id))");
        }
    }


    public void addUnlockable(String id) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO unlockables (id) VALUES (?) ON CONFLICT(id) DO NOTHING")) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        }
    }

    public void deleteUnlockable(String id) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM unlockables WHERE id = ?")) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        }
    }

    public void modifyUnlockable(UUID uuid, String id, Action action) throws SQLException {
        switch (action) {
            case GIVE:
                try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO playerdata_unlockables (uuid, id) VALUES (?, ?) ON CONFLICT(uuid, id) DO NOTHING")) {
                    stmt.setString(1, uuid.toString());
                    stmt.setString(2, id);
                    stmt.executeUpdate();
                }
                break;
            case TAKE:
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM playerdata_unlockables WHERE uuid = ? AND id = ?")) {
                    stmt.setString(1, uuid.toString());
                    stmt.setString(2, id);
                    stmt.executeUpdate();
                }
                break;
        }
    }

    public void removeAllTemps(UUID uuid) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM playerdata_unlockables WHERE uuid = ? AND temp = 1")) {
            stmt.setString(1, uuid.toString());
            stmt.executeUpdate();
        }
    }

    public boolean getPlayerUnlockable(OfflinePlayer p, String id) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM player_unlockables WHERE uuid = ? AND id = ? AND temp = 0")) {
            stmt.setString(1, p.getUniqueId().toString());
            stmt.setString(2, id);
            ResultSet resultSet = stmt.executeQuery();
            return resultSet.next();
        }
    }
    public boolean getPlayerTempUnlockable(OfflinePlayer p, String id) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM playerdata_unlockables WHERE uuid = ? AND id = ? AND temp = 1")) {
            stmt.setString(1, p.getUniqueId().toString());
            stmt.setString(2, id);
            ResultSet resultSet = stmt.executeQuery();
            return resultSet.next();
        }
    }

}
