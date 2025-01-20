package me.hektortm.woSSystems.database;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.dao.EconomyDAO;
import me.hektortm.woSSystems.database.dao.UnlockableDAO;
import me.hektortm.woSSystems.systems.unlockables.utils.Action;
import me.hektortm.woSSystems.utils.dataclasses.Currency;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.UUID;
import java.util.logging.Level;

public class DatabaseManager {
    private Connection connection;
    private EconomyDAO economyDAO;
    private UnlockableDAO unlockableDAO;
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);

    public DatabaseManager(String path) throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + path);

        this.economyDAO = new EconomyDAO(this);
        this.unlockableDAO = new UnlockableDAO(this);

        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS playerdata (" +
                    "uuid TEXT PRIMARY KEY, " +
                    "username TEXT NOT NULL)");
        }

        plugin.writeLog("DatabaseManager", Level.INFO, "Database initialized.");
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

    public Connection getConnection() {
        return connection;
    }

    public EconomyDAO getEconomyDAO() {
        return economyDAO;
    }

    public UnlockableDAO getUnlockableDAO() {
        return unlockableDAO;
    }

}
