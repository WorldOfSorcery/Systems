package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.WoSSystems;

import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.dataclasses.Currency;
import me.hektortm.wosCore.database.DatabaseManager;
import me.hektortm.wosCore.database.IDAO;
import me.hektortm.wosCore.discord.DiscordLog;
import me.hektortm.wosCore.discord.DiscordLogger;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class EconomyDAO implements IDAO {
    private final DatabaseManager db;
    private final DAOHub hub;
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final String logName = "EconomyDAO";

    public EconomyDAO(DatabaseManager db, DAOHub hub) throws SQLException {
        this.db = db;
        this.hub = hub;
    }

    @Override
    public void initializeTable() throws SQLException {
        try (Connection conn = db.getConnection(); Statement statement = conn.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS playerdata_economy(" +
                    "uuid CHAR(36), " +
                    "currency VARCHAR(200), " +
                    "amount BIGINT NOT NULL DEFAULT 0, " +
                    "PRIMARY KEY (uuid, currency), " +
                    "FOREIGN KEY (uuid) REFERENCES playerdata(uuid))");
            // economy currencies table
            statement.execute("CREATE TABLE IF NOT EXISTS currencies(" +
                    "id VARCHAR(255) NOT NULL, " +
                    "name VARCHAR(255) NOT NULL, " +
                    "short_name VARCHAR(255) NOT NULL, " +
                    "icon VARCHAR(255), "+
                    "color VARCHAR(255), "+
                    "hidden_if_zero BOOLEAN NOT NULL DEFAULT false)");
            statement.execute("CREATE TABLE IF NOT EXISTS eco_log (" +
                    "uuid CHAR(36) NOT NULL, " +
                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "currency VARCHAR(255)," +
                    "previous_amount BIGINT," +
                    "new_amount BIGINT," +
                    "change_amount BIGINT," +
                    "source_type VARCHAR(255)," +
                    "source VARCHAR(255)) ");
        }
    }

    public void ecoLog(UUID uuuid, String currency, long previousAmount, long newAmount, long changeAmount, String sourceType, String source) {
        try (Connection conn = db.getConnection(); PreparedStatement prepStmt = conn.prepareStatement(
                "INSERT INTO eco_log (uuid, currency, previous_amount, new_amount, change_amount,source_type, source) VALUES (?, ?, ?, ?, ?, ?, ?)"
        )) {
            prepStmt.setString(1, uuuid.toString());
            prepStmt.setString(2, currency);
            prepStmt.setLong(3, previousAmount);
            prepStmt.setLong(4, newAmount);
            prepStmt.setLong(5, changeAmount);
            prepStmt.setString(6, sourceType);
            prepStmt.setString(7, source);
            prepStmt.executeUpdate();
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "9e047279", "Failed to write eco log: ", e
            ));
        }
    }

    public void ensurePlayerEconomyEntry(Player player, String currency) {
        try (Connection conn = db.getConnection(); PreparedStatement prepStmt = conn.prepareStatement(
                "INSERT IGNORE INTO playerdata_economy (uuid, currency, amount) VALUES (?, ?, 0)"
        )) {
            prepStmt.setString(1, player.getUniqueId().toString());
            prepStmt.setString(2, currency);
            prepStmt.executeUpdate();
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "fa0f93cd", "Failed to ensure DB Entry: ", e
            ));
        }
    }


    public void updatePlayerCurrency(Player player, String currency, long amount) {
        ensurePlayerEconomyEntry(player, currency);
        try (Connection conn = db.getConnection(); PreparedStatement prepStmt = conn.prepareStatement("UPDATE playerdata_economy SET amount = ? WHERE uuid = ? AND currency = ?")) {
            prepStmt.setLong(1, amount);
            prepStmt.setString(2, player.getUniqueId().toString());
            prepStmt.setString(3, currency);
            prepStmt.executeUpdate();
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "59dfee1c", "Failed to update Player Currency("+currency+"): ", e
            ));
        }
    }

    public long getPlayerCurrency(Player player, String currency) {
        ensurePlayerEconomyEntry(player, currency);
        try (Connection conn = db.getConnection(); PreparedStatement prepStmt = conn.prepareStatement("SELECT amount FROM playerdata_economy WHERE uuid = ? AND currency = ?")) {
            prepStmt.setString(1, player.getUniqueId().toString());
            prepStmt.setString(2, currency);
            ResultSet resultSet = prepStmt.executeQuery();
            if (resultSet.next()) {
                return resultSet.getLong("amount");
            } else {
                return 0;
            }
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "27afb2aa", "Failed to get Player Currency("+currency+"): ", e
            ));
            return 0;
        }
    }

    public void addCurrency(String id, Currency currency) {
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO currencies (id, name, short_name, icon, color, hidden_if_zero) VALUES (?, ?, ?, ?, ?, ?)"
        )) {
            stmt.setString(1, id);
            stmt.setString(2, currency.getName());
            stmt.setString(3, currency.getShortName());
            stmt.setString(4, currency.getIcon());
            stmt.setString(5, currency.getColor());
            stmt.setBoolean(6, currency.isHiddenIfZero());
            stmt.executeUpdate();
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "cb77a2a2", "Failed to add Player Currency("+currency+"): ", e
            ));
        }
    }

    public Map<String, Currency> getCurrencies() {
        Map<String, Currency> currenciesMap = new HashMap<>();
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM currencies")) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String id = rs.getString("id");
                String name = rs.getString("name");
                String shortName = rs.getString("short_name");
                String icon = rs.getString("icon");
                String color = rs.getString("color");
                boolean hiddenIfZero = rs.getBoolean("hidden_if_zero");

                Currency currency = new Currency(name, shortName, icon, color, hiddenIfZero);
                currenciesMap.put(id, currency);
            }
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "10a473af", "Failed to get Currencies: ", e
            ));
        }
        return currenciesMap;
    }


    public boolean currencyExists(String id) {
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM currencies WHERE id = ?")) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "73701ec6", "Failed to check if Currency("+id+") exists: ", e
            ));
            return false;
        }
        return false;
    }



}
