package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DatabaseManager;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

public class PlayerDAO {
    private Connection connection;
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);

    public PlayerDAO(DatabaseManager databaseManager) {
        this.connection = databaseManager.getConnection();
        createTable();
    }

    private void createTable() {
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS playerdata (" +
                    "uuid TEXT PRIMARY KEY, " +
                    "username TEXT NOT NULL)");
        } catch (SQLException e) {
            plugin.writeLog("PlayerDAO", Level.SEVERE, "Error creating Tables: " + e.getMessage());
        }
    }

    public void addPlayer(Player player) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO playerdata (uuid, username) VALUES (?, ?) ON CONFLICT(uuid) DO NOTHING")) {
            preparedStatement.setString(1, player.getUniqueId().toString());
            preparedStatement.setString(2, player.getName());
            preparedStatement.executeUpdate();
        }
    }

}
