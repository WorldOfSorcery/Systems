package me.hektortm.woSSystems.economy;

import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.utils.dataclasses.Currency;
import me.hektortm.wosCore.WoSCore;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public class EcoManager {
    private final WoSCore core;
    private final WoSSystems plugin;
    private final DAOHub hub;
    private final Map<String, me.hektortm.woSSystems.utils.dataclasses.Currency> currencies = new HashMap<>();


    public EcoManager(WoSSystems plugin, DAOHub hub) {
        this.plugin = plugin;
        this.core = (WoSCore) plugin.getServer().getPluginManager().getPlugin("WoSCore");
        this.hub = hub;

        loadDefaultCurrencies();
        loadCurrencies();
    }

    public void modifyCurrency(UUID uuid, String currency, long amount, Operation operation) {
        Player player = plugin.getServer().getPlayer(uuid);
        if (player == null) return; // Ensure player is online

        try {
            long currentAmount = getCurrencyBalance(uuid, currency);
            long newAmount = currentAmount;

            switch (operation) {
                case GIVE:
                    newAmount += amount;
                    break;
                case TAKE:
                    newAmount = Math.max(0, currentAmount - amount);
                    break;
                case SET:
                    newAmount = amount;
                    break;
                case RESET:
                    newAmount = 0;
                    break;
            }
            hub.getEconomyDAO().updatePlayerCurrency(player, currency, newAmount);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public enum Operation {
        GIVE, TAKE, SET, RESET
    }

    public void loadCurrencies() {
        currencies.clear();
        try {
            ResultSet rs = hub.getEconomyDAO().getCurrencies(); // Assume this method returns all currencies from the database
            while (rs.next()) {
                String id = rs.getString("id");
                String name = rs.getString("name");
                String shortName = rs.getString("short_name");
                String icon = rs.getString("icon");
                String color = rs.getString("color");
                boolean hiddenIfZero = rs.getBoolean("hidden_if_zero");

                Currency currency = new Currency(name, shortName, icon, color, hiddenIfZero);
                currencies.put(id, currency);
            }
        } catch (SQLException e) {
            plugin.writeLog("EcoManager", Level.SEVERE, "Error loading currencies from the database: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void loadDefaultCurrencies() {
        try {
            if (!hub.getEconomyDAO().currencyExists("gold")) { // Check if 'gold' currency exists
                Currency gold = new Currency("Gold", "g", "", "§6", false);
                hub.getEconomyDAO().addCurrency("gold", gold); // Insert into the database
                currencies.put("gold", gold);
            }

            if (!hub.getEconomyDAO().currencyExists("signature_token")) { // Check if 'signature_token' exists
                Currency signature = new Currency("Signature Token", "st", "", "§e", true);
                hub.getEconomyDAO().addCurrency("signature_token", signature); // Insert into the database
                currencies.put("signature_token", signature);
            }
        } catch (SQLException e) {
            plugin.writeLog("EcoManager", Level.SEVERE, "Error loading default currencies: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public Map<String, Currency> getCurrencies() {
        return currencies;
    }

    public long getCurrencyBalance(UUID uuid, String currency) {
        Player player = plugin.getServer().getPlayer(uuid);
        if (player == null) return 0; // Default to 0 if player isn't found


        return hub.getEconomyDAO().getPlayerCurrency(player, currency);

    }

    public boolean hasEnoughCurrency(UUID uuid, String currency, long amount) {
        return getCurrencyBalance(uuid, currency) >= amount;
    }

}
