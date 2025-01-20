package me.hektortm.woSSystems.database;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.dao.EconomyDAO;
import me.hektortm.woSSystems.database.dao.PlayerDAO;
import me.hektortm.woSSystems.database.dao.StatsDAO;
import me.hektortm.woSSystems.database.dao.UnlockableDAO;
import me.hektortm.woSSystems.systems.unlockables.utils.Action;
import me.hektortm.woSSystems.utils.dataclasses.Currency;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.UUID;
import java.util.logging.Level;

public class DatabaseManager {
    private final Connection connection;
    private final EconomyDAO economyDAO;
    private final UnlockableDAO unlockableDAO;
    private final PlayerDAO playerDAO;
    private final StatsDAO statsDAO;
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);

    public DatabaseManager(String path) throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + path);

        this.economyDAO = new EconomyDAO(this);
        this.unlockableDAO = new UnlockableDAO(this);
        this.playerDAO = new PlayerDAO(this);
        this.statsDAO = new StatsDAO(this);

        plugin.writeLog("DatabaseManager", Level.INFO, "Database initialized.");
    }

    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            plugin.writeLog("DatabaseManager", Level.INFO, "Database closed.");
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
    public PlayerDAO getPlayerDAO() {
        return playerDAO;
    }
    public StatsDAO getStatsDAO() {
        return statsDAO;
    }

}
