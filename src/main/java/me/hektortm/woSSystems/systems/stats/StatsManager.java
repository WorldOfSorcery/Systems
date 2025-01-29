package me.hektortm.woSSystems.systems.stats;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.dataclasses.GlobalStat;
import me.hektortm.woSSystems.systems.stats.utils.Operation;
import me.hektortm.woSSystems.utils.dataclasses.Stat;
import me.hektortm.wosCore.Utils;
import me.hektortm.wosCore.WoSCore;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class StatsManager {

    private final Map<String, Stat> stats = new HashMap<>();
    private final Map<String, GlobalStat> globalStats = new HashMap<>();

    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final WoSCore core = plugin.getCore();
    private final DAOHub hub;

    public StatsManager(DAOHub hub) {
        this.hub = hub;
    }

    public void createStat(Stat stat) {
        try {
            hub.getStatsDAO().createStat(stat);
        } catch (Exception e) {
            plugin.writeLog("StatsManager", Level.SEVERE, "Error creating Stat: " + e.getMessage());
        }
    }

    public void createGlobalStat(GlobalStat globalStat) {
        try {
            hub.getStatsDAO().createGlobalStat(globalStat);
        } catch (Exception e) {
            plugin.writeLog("StatsManager", Level.SEVERE, "Error creating global Stat: " + e.getMessage());
        }
    }

    public void addStat(CommandSender sender, String id, long max, boolean capped, boolean global) {
        if (global) {
            if (getGlobalStats().containsKey(id)) {
                sender.sendMessage("Already exists");
                return;
            }
            GlobalStat stat = new GlobalStat(id.toLowerCase(), max, capped);
            createGlobalStat(stat);
        } else {
            if (getStats().containsKey(id)) {
                sender.sendMessage("Already exists");
                return;
            }
            Stat stat = new Stat(id.toLowerCase(), max, capped);
            createStat(stat);
        }
    }

    public void deleteStat(CommandSender sender, String id, boolean global) {
        try {
            if (global) {
                hub.getStatsDAO().deleteGlobalStat(id);
            } else {
                hub.getStatsDAO().deleteStat(id);
            }
        } catch (Exception e) {
            Utils.error(sender, "stats", "error.not-found");
        }
    }

    public void modifyStat(UUID uuid, String id, long amount, Operation operation) {
        try {
            hub.getStatsDAO().modifyPlayerStat(uuid, id, amount, operation);
        } catch (Exception e) {
            plugin.writeLog("StatsManager", Level.SEVERE, "Error modifying stat: " + e.getMessage());
        }
    }

    public void modifyGlobalStat(String id, long amount, Operation operation) {
        try {
            hub.getStatsDAO().modifyGlobalStatValue(id, amount, operation);
        } catch (Exception e) {
            plugin.writeLog("StatsManager", Level.SEVERE, "Error modifying global stat: " + e.getMessage());
        }
    }

    public void reloadAllStats() {
        // No longer needed since stats are always fetched from the database.
        Bukkit.getLogger().info("Reloading stats is not required with database integration.");
    }

    public void reloadStat(String id) {
        // Reloading a single stat is unnecessary when using the database.
        Bukkit.getLogger().info("Reloading a single stat is handled directly via the database.");
    }

    public Map<String, Stat> getStats() {
        return hub.getStatsDAO().getAllStats();
    }

    public Map<String, GlobalStat> getGlobalStats() {
        return hub.getStatsDAO().getAllGlobalStats();
    }
    /*
    public boolean isLimit(UUID uuid, String id, long amount) {
        try {
            return database.getStatsDAO().isPlayerStatLimitReached(uuid, id, amount);
        } catch (Exception e) {
            plugin.writeLog("StatsManager", Level.SEVERE, "Error checking player stat limit: " + e.getMessage());
            return false;
        }
    }

    public boolean isGlobalLimit(String id, long amount) {
        try {
            return database.getStatsDAO().isGlobalStatLimitReached(id, amount);
        } catch (Exception e) {
            plugin.writeLog("StatsManager", Level.SEVERE, "Error checking global stat limit: " + e.getMessage());
            return false;
        }
    }
     */
    public long getPlayerStat(UUID playerUUID, String statId) {
        try {
            return hub.getStatsDAO().getPlayerStatValue(playerUUID, statId);
        } catch (Exception e) {
            plugin.writeLog("StatsManager", Level.SEVERE, "Error fetching player stat value: " + e.getMessage());
            return 0;
        }
    }

    public long getGlobalStatValue(String id) {
        try {
            return hub.getStatsDAO().getGlobalStatValue(id);
        } catch (Exception e) {
            plugin.writeLog("StatsManager", Level.SEVERE, "Error fetching global stat value: " + e.getMessage());
            return 0;
        }
    }

    public long getStatMax(String statId) {
        try {
            Stat stat = getStats().get(statId);
            return stat != null ? stat.getMax() : 0;
        } catch (Exception e) {
            plugin.writeLog("StatsManager", Level.SEVERE, "Error fetching stat max: " + e.getMessage());
            return 0;
        }
    }

    public long getGlobalStatMax(String id) {
        try {
            GlobalStat stat = getGlobalStats().get(id);
            return stat != null ? stat.getMax() : 0;
        } catch (Exception e) {
            plugin.writeLog("StatsManager", Level.SEVERE, "Error fetching global stat max: " + e.getMessage());
            return 0;
        }
    }
}
