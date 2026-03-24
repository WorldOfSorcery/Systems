package me.hektortm.woSSystems.systems.stats;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.Operations;
import me.hektortm.woSSystems.utils.model.GlobalStat;
import me.hektortm.woSSystems.utils.model.Stat;
import org.bukkit.OfflinePlayer;

import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Service layer for the player stat and global stat systems.
 *
 * <p>Delegates all persistence and arithmetic to
 * {@link me.hektortm.woSSystems.database.dao.StatsDAO} via the {@link DAOHub}.
 * Errors are logged at SEVERE level but never propagated to callers; on failure
 * the method returns a safe default (typically {@code 0} or {@code false}).</p>
 */
public class StatsManager {

    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final DAOHub hub;
    private final String logName = "StatsManager";

    /**
     * @param hub the DAO hub used to access stat persistence
     */
    public StatsManager(DAOHub hub) {
        this.hub = hub;
    }

    /**
     * Applies an arithmetic operation to a player's stat value.
     *
     * @param uuid      the player's UUID
     * @param id        the stat definition ID
     * @param amount    the operand value
     * @param operation the operation to apply (give/take/set/reset)
     */
    public void modifyStat(UUID uuid, String id, long amount, Operations operation) {
        try {
            hub.getStatsDAO().modifyPlayerStat(uuid, id, amount, operation);
        } catch (Exception e) {
            plugin.writeLog(logName, Level.SEVERE, "Error modifying stat: " + e);
        }
    }

    /**
     * Applies an arithmetic operation to a global stat value.
     *
     * @param id        the global stat definition ID
     * @param amount    the operand value
     * @param operation the operation to apply (give/take/set/reset)
     */
    public void modifyGlobalStat(String id, long amount, Operations operation) {
        try {
            hub.getStatsDAO().modifyGlobalStatValue(id, amount, operation);
        } catch (Exception e) {
            plugin.writeLog(logName, Level.SEVERE, "Error modifying global stat: " + e);
        }
    }

    /**
     * Returns all player stat definitions.
     *
     * @return map of stat ID to {@link Stat} definition
     */
    public Map<String, Stat> getStats() {
        return hub.getStatsDAO().getAllStats();
    }

    /**
     * Returns all global stat definitions.
     *
     * @return map of global stat ID to {@link GlobalStat} definition
     */
    public Map<String, GlobalStat> getGlobalStats() {
        return hub.getStatsDAO().getAllGlobalStats();
    }

    /**
     * Returns {@code true} if the player's value for the given stat has reached
     * or exceeded the configured maximum (only relevant for capped stats).
     *
     * @param uuid the player's UUID
     * @param id   the stat ID
     * @return {@code true} if the cap is reached; {@code false} on error or if uncapped
     */
    public boolean isLimit(UUID uuid, String id) {
        try {
            return hub.getStatsDAO().isStatLimitReached(uuid, id);
        } catch (Exception e) {
            plugin.writeLog(logName, Level.SEVERE, "Error checking player stat limit: " + e);
            return false;
        }
    }

    /**
     * Returns {@code true} if the global stat has reached or exceeded its cap.
     *
     * @param id the global stat ID
     * @return {@code true} if the cap is reached; {@code false} on error or if uncapped
     */
    public boolean isGlobalLimit(String id) {
        try {
            return hub.getStatsDAO().isGlobalStatLimitReached(id);
        } catch (Exception e) {
            plugin.writeLog(logName, Level.SEVERE, "Error checking global stat limit: " + e);
            return false;
        }
    }

    /**
     * Returns the player's current value for the given stat.
     *
     * @param playerUUID the player's UUID
     * @param statId     the stat ID
     * @return the current value, or {@code 0} on error
     */
    public long getPlayerStat(UUID playerUUID, String statId) {
        try {
            return hub.getStatsDAO().getPlayerStatValue(playerUUID, statId);
        } catch (Exception e) {
            plugin.writeLog(logName, Level.SEVERE, "Error fetching player stat value: " + e);
            return 0;
        }
    }

    /**
     * Returns the current value of the given global stat.
     *
     * @param id the global stat ID
     * @return the current value, or {@code 0} on error
     */
    public long getGlobalStatValue(String id) {
        try {
            return hub.getStatsDAO().getGlobalStatValue(id);
        } catch (Exception e) {
            plugin.writeLog(logName, Level.SEVERE, "Error fetching global stat value: " + e);
            return 0;
        }
    }

    /**
     * Returns the configured maximum value for the given stat definition.
     *
     * @param statId the stat ID
     * @return the max value, or {@code 0} if the stat is not found or on error
     */
    public long getStatMax(String statId) {
        try {
            Stat stat = getStats().get(statId);
            return stat != null ? stat.getMax() : 0;
        } catch (Exception e) {
            plugin.writeLog(logName, Level.SEVERE, "Error fetching stat max: " + e);
            return 0;
        }
    }

    /**
     * Returns {@code true} if the player's value for the given stat is at least
     * {@code amount}.
     *
     * @param player the offline player to check
     * @param id     the stat ID
     * @param amount the minimum required value
     * @return {@code true} if the player meets the requirement
     */
    public boolean hasStatValue(OfflinePlayer player, String id, long amount) {
        UUID playerUUID = player.getUniqueId();
        long statValue = hub.getStatsDAO().getPlayerStatValue(playerUUID, id);
        return statValue >= amount;
    }

    /**
     * Returns the configured maximum value for the given global stat definition.
     *
     * @param id the global stat ID
     * @return the max value, or {@code 0} if the stat is not found or on error
     */
    public long getGlobalStatMax(String id) {
        try {
            GlobalStat stat = getGlobalStats().get(id);
            return stat != null ? stat.getMax() : 0;
        } catch (Exception e) {
            plugin.writeLog(logName, Level.SEVERE, "Error fetching global stat max: " + e);
            return 0;
        }
    }
}
