package me.hektortm.woSSystems.Database;

import me.hektortm.woSSystems.systems.unlockables.utils.Action;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.UUID;

import static me.hektortm.woSSystems.systems.unlockables.utils.Action.GIVE;

public class DatabaseManager {
    private final Connection connection;

    public DatabaseManager(String path) throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + path);
        try (Statement statement = connection.createStatement()) {
            // playerdata table
            statement.execute("CREATE TABLE IF NOT EXISTS playerdata (" +
                    "uuid TEXT PRIMARY KEY, " +
                    "username TEXT NOT NULL)");
            // playerdata economy table
            statement.execute("CREATE TABLE IF NOT EXISTS playerdata_economy(" +
                    "uuid TEXT, " +
                    "currency TEXT, " +
                    "amount LONG NOT NULL DEFAULT 0, " +
                    "PRIMARY KEY (uuid, currency), " +
                    "FOREIGN KEY (uuid) REFERENCES playerdata(uuid))");
            // economy currencies table
            statement.execute("CREATE TABLE IF NOT EXISTS currencies(" +
                    "id TEXT NOT NULL, " +
                    "name TEXT NOT NULL, " +
                    "short_name TEXT NOT NULL, " +
                    "icon TEXT, "+ "color TEXT)");
            // unlockables table
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
        }
    }

    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    public void addPlayer(Player player) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO playerdata (uuid, username) VALUES (?, ?) ON CONFLICT(uuid) DO NOTHING")) {
            preparedStatement.setString(1, player.getUniqueId().toString());
            preparedStatement.setString(2, player.getName());
            preparedStatement.executeUpdate();
        }
    }

    public void ensurePlayerEconomyEntry(Player player, String currency) throws SQLException {
        addPlayer(player);
        try (PreparedStatement prepStmt = connection.prepareStatement("INSERT INTO playerdata_economy (uuid, currency, amount) VALUES (?, ?, 0) ON CONFLICT(uuid, currency) DO NOTHING")) {
            prepStmt.setString(1, player.getUniqueId().toString());
            prepStmt.setString(2, currency);
            prepStmt.executeUpdate();
        }
    }

    public void updatePlayerCurrency(Player player, String currency, long amount) throws SQLException {
        ensurePlayerEconomyEntry(player, currency);
        try (PreparedStatement prepStmt = connection.prepareStatement("UPDATE playerdata_economy SET amount = ? WHERE uuid = ? AND currency = ?")) {
            prepStmt.setLong(1, amount);
            prepStmt.setString(2, player.getUniqueId().toString());
            prepStmt.setString(3, currency);
            prepStmt.executeUpdate();
        }
    }

    public long getPlayerCurrency(Player player, String currency) throws SQLException {
        ensurePlayerEconomyEntry(player, currency);
        try (PreparedStatement prepStmt = connection.prepareStatement("SELECT amount FROM playerdata_economy WHERE uuid = ? AND currency = ?")) {
            prepStmt.setString(1, player.getUniqueId().toString());
            prepStmt.setString(2, currency);
            ResultSet resultSet = prepStmt.executeQuery();
            if (resultSet.next()) {
                return resultSet.getLong("amount");
            } else {
                return 0;
            }
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




    public void addCurrency(String currencyId, String name, String shortName, String icon, String color) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO currencies (currency_id, name, short_name, icon, color) VALUES (?, ?, ?, ?, ?) ON CONFLICT(currency_id) DO NOTHING")) {
            preparedStatement.setString(1, currencyId);
            preparedStatement.setString(2, name);
            preparedStatement.setString(3, shortName);
            preparedStatement.setString(4, icon);
            preparedStatement.setString(5, color);
            preparedStatement.executeUpdate();
        }
    }

}
