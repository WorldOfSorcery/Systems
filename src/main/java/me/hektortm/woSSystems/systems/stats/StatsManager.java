package me.hektortm.woSSystems.systems.stats;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.Operations;
import me.hektortm.woSSystems.utils.dataclasses.GlobalStat;
import me.hektortm.woSSystems.utils.dataclasses.Stat;
import me.hektortm.wosCore.WoSCore;
import org.bukkit.OfflinePlayer;

import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class StatsManager {

    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final DAOHub hub;
    private final String logName = "StatsManager";

    public StatsManager(DAOHub hub) {
        this.hub = hub;
    }

    public void modifyStat(UUID uuid, String id, long amount, Operations operation) {
        try {
            hub.getStatsDAO().modifyPlayerStat(uuid, id, amount, operation);
        } catch (Exception e) {
            plugin.writeLog(logName, Level.SEVERE, "Error modifying stat: " + e.getMessage());
        }
    }

    public void modifyGlobalStat(String id, long amount, Operations operation) {
        try {
            hub.getStatsDAO().modifyGlobalStatValue(id, amount, operation);
        } catch (Exception e) {
            plugin.writeLog(logName, Level.SEVERE, "Error modifying global stat: " + e.getMessage());
        }
    }

    public Map<String, Stat> getStats() {
        return hub.getStatsDAO().getAllStats();
    }

    public Map<String, GlobalStat> getGlobalStats() {
        return hub.getStatsDAO().getAllGlobalStats();
    }

    public boolean isLimit(UUID uuid, String id) {
        try {
            return hub.getStatsDAO().isStatLimitReached(uuid, id);
        } catch (Exception e) {
            plugin.writeLog(logName, Level.SEVERE, "Error checking player stat limit: " + e.getMessage());
            return false;
        }
    }

    public boolean isGlobalLimit(String id) {
        try {
            return hub.getStatsDAO().isGlobalStatLimitReached(id);
        } catch (Exception e) {
            plugin.writeLog(logName, Level.SEVERE, "Error checking global stat limit: " + e.getMessage());
            return false;
        }
    }

    public long getPlayerStat(UUID playerUUID, String statId) {
        try {
            return hub.getStatsDAO().getPlayerStatValue(playerUUID, statId);
        } catch (Exception e) {
            plugin.writeLog(logName, Level.SEVERE, "Error fetching player stat value: " + e.getMessage());
            return 0;
        }
    }

    public long getGlobalStatValue(String id) {
        try {
            return hub.getStatsDAO().getGlobalStatValue(id);
        } catch (Exception e) {
            plugin.writeLog(logName, Level.SEVERE, "Error fetching global stat value: " + e.getMessage());
            return 0;
        }
    }

    public long getStatMax(String statId) {
        try {
            Stat stat = getStats().get(statId);
            return stat != null ? stat.getMax() : 0;
        } catch (Exception e) {
            plugin.writeLog(logName, Level.SEVERE, "Error fetching stat max: " + e.getMessage());
            return 0;
        }
    }

    public boolean hasStatValue(OfflinePlayer player, String id, long amount) {
        UUID playerUUID = player.getUniqueId();
        long statValue = hub.getStatsDAO().getPlayerStatValue(playerUUID, id);
        return statValue >= amount;
    }

    public long getGlobalStatMax(String id) {
        try {
            GlobalStat stat = getGlobalStats().get(id);
            return stat != null ? stat.getMax() : 0;
        } catch (Exception e) {
            plugin.writeLog(logName, Level.SEVERE, "Error fetching global stat max: " + e.getMessage());
            return 0;
        }
    }
}
