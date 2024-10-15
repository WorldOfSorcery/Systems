package me.hektortm.woSSystems.stats;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.stats.utils.GlobalStat;
import me.hektortm.woSSystems.stats.utils.Operation;
import me.hektortm.woSSystems.stats.utils.Stat;
import me.hektortm.wosCore.Utils;
import me.hektortm.wosCore.WoSCore;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StatsManager {

    public File statsFolder = new File(WoSSystems.getPlugin(WoSSystems.class).getDataFolder(), "stats");
    public File globalStatsFolder = new File(WoSSystems.getPlugin(WoSSystems.class).getDataFolder(), "globalstats");
    public File globalStatsData = new File(WoSSystems.getPlugin(WoSSystems.class).getDataFolder(), "global_stats_data.yml");
    private final Map<String, Stat> stats = new HashMap<>();
    private final Map<String, GlobalStat> globalStats = new HashMap<>();

    private final WoSCore core;

    public StatsManager(WoSCore core) {
        this.core = core;

        loadStats();
        loadGlobalStats();
    }

    public void createStat(Stat stat) {
        File file = new File(statsFolder, stat.getId().toLowerCase() + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        config.set("id", stat.getId());
        config.set("capped", stat.getCapped());
        config.set("max", stat.getMax());

        try {
            config.save(file);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Could not save stat file for " + stat.getId());
        }
    }

    public void createGlobalStat(GlobalStat globalStat) {
        File file = new File(globalStatsFolder, globalStat.getId().toLowerCase() + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        config.set("id", globalStat.getId());
        config.set("capped", globalStat.getCapped());
        config.set("max", globalStat.getMax());

        try {
            config.save(file);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Could not save global stat file for " + globalStat.getId());
        }
    }

    public void addStat(CommandSender sender, String id, long max, boolean capped, boolean global) {
        if (global) {
            if (globalStats.containsKey(id)) {
                sender.sendMessage("Already exists");
                return;
            }

            GlobalStat stat = new GlobalStat(id.toLowerCase(), max, capped);
            globalStats.put(id.toLowerCase(), stat);

            createGlobalStat(stat);

        } else {
            if (stats.containsKey(id)) {
                sender.sendMessage("Already exists");
                return;
            }

            Stat stat = new Stat(id.toLowerCase(), max, capped);
            stats.put(id.toLowerCase(), stat);

            createStat(stat);
        }
    }

    public void deleteStat(CommandSender sender, String id, boolean global) {
        if (global) {
            if (!globalStats.containsKey(id)) {
                Utils.error(sender, "stats", "error.not-found");
            }

            File file = new File(globalStatsFolder, id + ".yml");
            file.delete();
        } else {
            if (!stats.containsKey(id)) {
                Utils.error(sender, "stats", "error.not-found");
            }

            File file = new File(statsFolder, id + ".yml");
            file.delete();
        }
    }


    public void modifyStat(UUID uuid, String id, long amount, Operation operation) {
        OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
        FileConfiguration playerData = core.getPlayerData(uuid, p.getName());
        File playerFile = new File(core.getDataFolder(), "playerdata" + File.separator + uuid + ".yml");

        String path = "stats." + id.toLowerCase();

        Stat stat = stats.get(id.toLowerCase());
        long max = stat.getMax();
        int statAmount = playerData.getInt(path, 0);
        boolean capped = stat.getCapped();



        switch (operation) {
            case GIVE:
                if (isLimit(uuid, id.toLowerCase(), amount)) {
                    playerData.set(path, max);
                    break;
                }
                playerData.set(path, statAmount + amount);
                break;
            case TAKE:
                if ((statAmount - amount) < 0) {
                    playerData.set(path, 0);
                    break;
                }
                playerData.set(path, statAmount - amount);
                break;
            case SET:
                if (capped) {
                    if (amount > max) {
                        playerData.set(path, max);
                        break;
                    }
                }

                playerData.set(path, amount);
                break;
            case RESET:
                playerData.set(path, 0);
                break;
        }
        core.savePlayerData(playerData, playerFile);
    }

    public void modifyGlobalStat(String id, long amount, Operation operation) {

        File dataFile = globalStatsData;
        FileConfiguration config = YamlConfiguration.loadConfiguration(dataFile);

        String path = "globalstats." + id.toLowerCase();

        GlobalStat stat = globalStats.get(id.toLowerCase());
        long max = stat.getMax();
        int statAmount = config.getInt(path, 0);
        boolean capped = stat.getCapped();



        switch (operation) {
            case GIVE:
                if (isGlobalLimit(id, amount)) {
                    config.set(path, max);
                    break;
                }
                config.set(path, statAmount + amount);
                break;
            case TAKE:
                if ((statAmount - amount) < 0) {
                    config.set(path, 0);
                    break;
                }
                config.set(path, statAmount - amount);
                break;
            case SET:
                if (capped) {
                    if (amount > max) {
                        config.set(path, max);
                        break;
                    }
                }

                config.set(path, amount);
                break;
            case RESET:
                config.set(path, 0);
                break;
        }
        try {
            config.save(dataFile);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Could not save Global stat value for: "+id);
        }

    }

    public void reloadAllStats() {
        stats.clear(); // Clear current stats
        loadStats();   // Reload all stats from files
        Bukkit.getLogger().info("All stats reloaded.");
    }

    // Reload a single stat
    public void reloadStat(String id) {
        File file = new File(statsFolder, id.toLowerCase() + ".yml");
        if (!file.exists()) {
            Bukkit.getLogger().severe("Stat file for " + id + " does not exist.");
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        String statId = config.getString("id");
        long max = config.getInt("max");
        boolean capped = config.getBoolean("capped");

        Stat stat = new Stat(statId, max, capped);
        stats.put(statId.toLowerCase(), stat); // Update or insert stat in the map
        Bukkit.getLogger().info("Stat " + id + " reloaded.");
    }

    public void loadStats() {
        if(!statsFolder.exists()) {
            statsFolder.mkdir();
        }

        File[] files = statsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if(files != null) {
            for (File file : files) {
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);

                String id = config.getString("id");
                long max = config.getInt("max");
                boolean capped = config.getBoolean("capped");

                Stat stat = new Stat(id, max, capped);
                stats.put(id.toLowerCase(), stat);
            }
        }
    }
    public void loadGlobalStats() {
        if(!globalStatsFolder.exists()) {
            globalStatsFolder.mkdir();
        }

        File[] files = globalStatsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if(files != null) {
            for (File file : files) {
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                String id = config.getString("id");
                long max = config.getInt("max");
                boolean capped = config.getBoolean("capped");

                GlobalStat stat = new GlobalStat(id, max, capped);
                globalStats.put(id.toLowerCase(), stat);
            }
        }
    }

    public Map<String, Stat> getStats() {
        return stats;
    }

    public Map<String, GlobalStat> getGlobalStats() {
        return globalStats;
    }

    public boolean isLimit(UUID uuid, String id, long amount) {
        Stat stat = stats.get(id.toLowerCase());
        File playerFile = new File(core.getDataFolder(), "playerdata" + File.separator + uuid.toString() + ".yml");
        FileConfiguration playerData = YamlConfiguration.loadConfiguration(playerFile);

        long max = stat.getMax();
        boolean capped = stat.getCapped();
        String path = "stats." + id.toLowerCase();
        long pStatValue = playerData.getLong(path, 0);

        if (!capped) {
            return false;
        }

        long finalAmount = pStatValue + amount;
        return finalAmount > max;

    }

    public boolean isGlobalLimit(String id, long amount) {
        GlobalStat stat = globalStats.get(id.toLowerCase());
        File dataFile = globalStatsData;
        FileConfiguration config = YamlConfiguration.loadConfiguration(dataFile);

        long max = stat.getMax();
        boolean capped = stat.getCapped();
        String path = "globalstats." + id.toLowerCase();
        long statValue = config.getLong(path, 0);

        if (!capped) {
            return false;
        }

        long finalAmount = statValue + amount;
        return finalAmount > max;
    }

    public long getPlayerStat(UUID playerUUID, String statId) {
        File playerFile = new File(core.getDataFolder(), "playerdata" + File.separator + playerUUID.toString() + ".yml");
        FileConfiguration playerData = YamlConfiguration.loadConfiguration(playerFile);

        String path = "stats." + statId.toLowerCase();
        return playerData.getLong(path, 0);
    }

    public long getGlobalStat(String id) {
        File dataFile = globalStatsData;
        FileConfiguration config = YamlConfiguration.loadConfiguration(dataFile);

        String path = "globalstats." + id.toLowerCase();
        return config.getLong(path, 0);
    }

    public long getStatMax(String statId) {
        Stat stat = stats.get(statId.toLowerCase());
        return stat != null ? stat.getMax() : 0;
    }

    public long getGlobalStatMax(String id) {
        GlobalStat stat = globalStats.get(id.toLowerCase());
        return stat != null ? stat.getMax() : 0;
    }



}
