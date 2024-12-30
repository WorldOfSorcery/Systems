package me.hektortm.woSSystems.economy;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.utils.dataclasses.Currency;
import me.hektortm.wosCore.WoSCore;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class EcoManager {
    private final WoSCore core;
    private final WoSSystems plugin;
    public final File currencyDirectory;
    private final Map<String, me.hektortm.woSSystems.utils.dataclasses.Currency> currencies = new HashMap<>();


    public EcoManager(WoSSystems plugin) {
        this.plugin = plugin;
        this.currencyDirectory = new File(plugin.getDataFolder(), "currencies");
        this.core = (WoSCore) plugin.getServer().getPluginManager().getPlugin("WoSCore");

        loadDefaultCurrencies();
        loadCurrencies();
    }

    public void modifyCurrency(UUID uuid, String currencyID, long amount, Operation operation) {
        Player player = plugin.getServer().getPlayer(uuid);


        assert player != null;
        FileConfiguration playerData = core.getPlayerData(uuid, player.getName());
        File playerFile = new File(core.getDataFolder() ,"playerdata" + File.separator + uuid + ".yml");

        String path = "economy." + currencyID;
        long currentAmount = playerData.getLong(path, 0);

        switch (operation) {
            case GIVE:
                playerData.set(path, currentAmount + amount);
                break;
            case TAKE:
                playerData.set(path, currentAmount - amount);
                break;
            case SET:
                playerData.set(path, amount);
                break;
            case RESET:
                playerData.set(path, 0);
                break;

        }
        core.savePlayerData(playerData, playerFile);

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

    public long getCurrencyBalance(UUID uuid, String currencyName) {
        File playerFile = new File(core.getDataFolder(), "playerdata" + File.separator + uuid.toString() + ".yml");
        FileConfiguration playerData = YamlConfiguration.loadConfiguration(playerFile);

        String path = "economy."+currencyName;

        return playerData.getLong(path, 0);
    }

    public boolean hasEnoughCurrency(UUID uuid, String currencyName, long amount) {
        long currentBalance = getCurrencyBalance(uuid, currencyName);
        return currentBalance >= amount;
    }

}
