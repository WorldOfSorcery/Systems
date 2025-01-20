package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DatabaseManager;
import me.hektortm.woSSystems.utils.dataclasses.Currency;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.logging.Level;

public class EconomyDAO {
    private final Connection connection;
    private final DatabaseManager databaseManager;
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);

    public EconomyDAO(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        this.connection = databaseManager.getConnection();
        createTables();
    }


    private void createTables() {
        try (Statement statement = connection.createStatement()) {
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
                    "icon TEXT, "+
                    "color TEXT, "+
                    "hidden_if_zero BOOLEAN NOT NULL DEFAULT false)");
        } catch (SQLException e) {
            plugin.writeLog("EconomyDAO", Level.SEVERE, "Error creating Tables: " + e.getMessage());
        }
    }

    public void ensurePlayerEconomyEntry(Player player, String currency) throws SQLException {
        databaseManager.getPlayerDAO().addPlayer(player);
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

    public void addCurrency(String id, Currency currency) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO currencies (id, name, short_name, icon, color, hidden_if_zero) VALUES (?, ?, ?, ?, ?, ?)"
        );
        stmt.setString(1, id);
        stmt.setString(2, currency.getName());
        stmt.setString(3, currency.getShortName());
        stmt.setString(4, currency.getIcon());
        stmt.setString(5, currency.getColor());
        stmt.setBoolean(6, currency.isHiddenIfZero());
        stmt.executeUpdate();
    }

    public ResultSet getCurrencies() throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT * FROM currencies");
        return stmt.executeQuery();
    }

    public boolean currencyExists(String id) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT COUNT(*) FROM currencies WHERE id = ?");
        stmt.setString(1, id);
        ResultSet rs = stmt.executeQuery();
        return rs.next() && rs.getInt(1) > 0;
    }


}
