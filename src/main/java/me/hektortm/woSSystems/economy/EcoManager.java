package me.hektortm.woSSystems.economy;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.wosCore.WoSCore;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

public class EcoManager {
    private final WoSCore core;
    private final WoSSystems plugin;
    private final File currencyDirectory;
    private final Map<String, Currency> currencies = new HashMap<>();


    public EcoManager(WoSSystems plugin) {
        this.plugin = plugin;
        this.currencyDirectory = new File(plugin.getDataFolder(), "currencies");
        this.core = (WoSCore) plugin.getServer().getPluginManager().getPlugin("WoSCore");


        loadCurrencies();
    }

    public void modifyCurrency(UUID uuid, String currencyName, int amount, Operation operation) {
        Player player = plugin.getServer().getPlayer(uuid);


        FileConfiguration playerData = core.getPlayerData(uuid, player.getName());
        File playerFile = new File(core.getDataFolder() ,"playerdata" + File.separator + uuid.toString() + ".yml");

        String path = "economy." + currencyName;
        int currentAmount = playerData.getInt(path, 0);

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

    public void loadCurrencies() {
        if (!currencyDirectory.exists()) {
            currencyDirectory.mkdirs();
        }

        File[] files = currencyDirectory.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);

                String name = config.getString("name");
                String shortName = config.getString("short");
                String icon = config.getString("icon");
                String color = config.getString("color");

                Currency currency = new Currency(name, shortName, icon, color);
                currencies.put(name.toLowerCase(), currency);
            }
        }
    }

    public Map<String, Currency> getCurrencies() {
        return currencies;
    }
    public List<String> getAllCurrencyNames() {
        // Assuming currencies is a Map<String, Currency> or similar structure that stores your currencies
        return new ArrayList<>(currencies.keySet());
    }

    public int getCurrencyBalance(UUID uuid, String currencyName) {
        File playerFile = new File(core.getDataFolder(), "playerdata" + File.separator + uuid.toString() + ".yml");
        FileConfiguration playerData = YamlConfiguration.loadConfiguration(playerFile);

        String path = "economy."+currencyName;

        return playerData.getInt(path, 0);
    }

    public boolean hasEnoughCurrency(UUID uuid, String currencyName, int amount) {
        int currentBalance = getCurrencyBalance(uuid, currencyName);
        return currentBalance >= amount;
    }

}
