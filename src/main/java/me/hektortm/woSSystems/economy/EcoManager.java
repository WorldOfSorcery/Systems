package me.hektortm.woSSystems.economy;

import me.hektortm.woSSystems.Database.DatabaseManager;
import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.utils.dataclasses.Currency;
import me.hektortm.wosCore.WoSCore;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public class EcoManager {
    private final WoSCore core;
    private final WoSSystems plugin;
    private final DatabaseManager database;
    public final File currencyDirectory;
    private final Map<String, me.hektortm.woSSystems.utils.dataclasses.Currency> currencies = new HashMap<>();


    public EcoManager(WoSSystems plugin, DatabaseManager database) {
        this.plugin = plugin;
        this.currencyDirectory = new File(plugin.getDataFolder(), "currencies");
        this.core = (WoSCore) plugin.getServer().getPluginManager().getPlugin("WoSCore");
        this.database = database;

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
            database.updatePlayerCurrency(player, currency, newAmount);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public enum Operation {
        GIVE, TAKE, SET, RESET
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void loadCurrencies() {
        if (!currencyDirectory.exists()) currencyDirectory.mkdir();

        File[] files = currencyDirectory.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);

                String id = file.getName().replace(".yml", "");
                String name = config.getString("name");
                String shortName = config.getString("short");
                String icon = config.getString("icon");
                String color = config.getString("color");

                me.hektortm.woSSystems.utils.dataclasses.Currency currency = new me.hektortm.woSSystems.utils.dataclasses.Currency(name, shortName, icon, color);
                assert name != null;
                currencies.put(id, currency);
            }
        }
    }

    private void loadDefaultCurrencies() {
        if (!currencyDirectory.exists()) currencyDirectory.mkdir();
        File goldFile = new File(currencyDirectory, "gold.yml");
        File signatureFile = new File(currencyDirectory, "signature_token.yml");
        if (!goldFile.exists()) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(goldFile);
            String id = goldFile.getName().replace(".yml", "");
            String name = "Gold";
            String shortName = "g";
            String icon = "";
            String color = "§6";

            config.set("name", name);
            config.set("short", shortName);
            config.set("icon", icon);
            config.set("color", color);
            try {
                config.save(goldFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Currency currency = new Currency(name, shortName, icon, color);
            currencies.put(id, currency);
        }
        if (!signatureFile.exists()) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(signatureFile);
            String id = signatureFile.getName().replace(".yml", "");
            String name = "Signature Token";
            String shortName = "st";
            String icon = "";
            String color = "§e";

            config.set("name", name);
            config.set("short", shortName);
            config.set("icon", icon);
            config.set("color", color);
            try {
                config.save(signatureFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Currency currency = new Currency(name, shortName, icon, color);
            currencies.put(id, currency);
        }


    }

    public Map<String, Currency> getCurrencies() {
        return currencies;
    }

    public long getCurrencyBalance(UUID uuid, String currency) {
        Player player = plugin.getServer().getPlayer(uuid);
        if (player == null) return 0; // Default to 0 if player isn't found

        try {
            return database.getPlayerCurrency(player, currency);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public boolean hasEnoughCurrency(UUID uuid, String currency, long amount) {
        return getCurrencyBalance(uuid, currency) >= amount;
    }

}
