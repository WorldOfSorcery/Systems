package me.hektortm.woSSystems.systems.cooldowns;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.model.Cooldown;
import me.hektortm.woSSystems.utils.model.InteractionKey;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Periodic task that polls all active cooldowns once per second and removes
 * any that have expired.
 *
 * <p>Extends {@link BukkitRunnable} so it can be scheduled as a repeating
 * Bukkit task via {@link #start()}.  The heavy database work is performed
 * asynchronously; only the expiry callback ({@link #onCooldownExpire}) is
 * dispatched back onto the main server thread so it can safely call the
 * {@link me.hektortm.woSSystems.systems.interactions.InteractionManager}.</p>
 */
public class CooldownManager extends BukkitRunnable {
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final DAOHub hub;

    /**
     * @param hub the DAO hub used to access cooldown persistence
     */
    public CooldownManager(DAOHub hub) {
        this.hub = hub;
    }

    /**
     * Tick body — executed once per second on a background thread.
     *
     * <p>Fetches all active global and local cooldowns from the database,
     * checks each one for expiry, removes expired entries, and schedules
     * {@link #onCooldownExpire} on the main thread for each removal.</p>
     */
    @Override
    public void run() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // Get all active cooldowns from database
                Map<UUID, List<String>> activeCooldowns = hub.getCooldownDAO().getAllActiveCooldowns();
                Map<UUID, Map<String, String>> activeLocalCooldowns = hub.getCooldownDAO().getAllActiveLocalCooldowns();

                // Process each player's cooldowns
                for (Map.Entry<UUID, List<String>> entry : activeCooldowns.entrySet()) {
                    UUID playerId = entry.getKey();
                    OfflinePlayer oP = Bukkit.getOfflinePlayer(playerId);

                    // Check each cooldown
                    for (String cooldownId : entry.getValue()) {
                        if (hub.getCooldownDAO().isCooldownExpired(oP, cooldownId)) {
                            // Remove expired cooldown
                            hub.getCooldownDAO().removeCooldown(oP, cooldownId);

                            // Trigger completion action on main thread
                            final OfflinePlayer finalOP = oP;
                            final String finalCooldownId = cooldownId;
                            Bukkit.getScheduler().runTask(plugin, () -> onCooldownExpire(finalOP, finalCooldownId));
                        }
                    }
                }
                for (Map.Entry<UUID, Map<String, String>> entry : activeLocalCooldowns.entrySet()) {
                    UUID playerId = entry.getKey();
                    OfflinePlayer oP = Bukkit.getOfflinePlayer(playerId);

                    for (Map.Entry<String, String> cdEntry : entry.getValue().entrySet()) {
                        String cooldownId = cdEntry.getKey();
                        String interactionKey = cdEntry.getValue();

                        if (hub.getCooldownDAO().isLocalCooldownExpired(oP, cooldownId, new InteractionKey(interactionKey))) {
                            hub.getCooldownDAO().removeLocalCooldown(oP, cooldownId, new InteractionKey(interactionKey));

                            final OfflinePlayer finalOP = oP;
                            final String finalCooldownId = cooldownId;
                            Bukkit.getScheduler().runTask(plugin, () -> onCooldownExpire(finalOP, finalCooldownId));
                        }
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Error processing cooldowns: " + e.getMessage());
            }
        });
    }

    /**
     * Called on the main thread when a cooldown expires.
     *
     * <p>Looks up the {@link Cooldown} definition and, if an
     * {@code end_interaction} ID is configured, triggers that interaction for
     * the player (provided they are currently online).</p>
     *
     * @param player     the player whose cooldown expired
     * @param cooldownId the expired cooldown's definition ID
     */
    private void onCooldownExpire(OfflinePlayer player, String cooldownId) {
        // Customize these actions based on your cooldown types
        plugin.writeLog("CooldownManager", Level.INFO, "Cooldown expired for player: " + player.getName() + " with ID: " + cooldownId);
        Cooldown cd = hub.getCooldownDAO().getCooldown(cooldownId);
        String endInt = cd.getEnd_interaction();

        if (endInt != null) {
            Player online = player.getPlayer();
            if (online != null) plugin.getInteractionManager().triggerInteraction(endInt, online, null);
        }

    }

    /**
     * Schedules this task to run on the Bukkit scheduler every second
     * (20 ticks), starting immediately.
     */
    public void start() {
        // Run every second (20 ticks)
        this.runTaskTimer(plugin, 0, 20);
    }

}
