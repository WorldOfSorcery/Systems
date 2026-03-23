package me.hektortm.woSSystems.systems.cooldowns;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.dataclasses.Cooldown;
import me.hektortm.woSSystems.utils.dataclasses.InteractionKey;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class CooldownManager extends BukkitRunnable {
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final DAOHub hub;

    public CooldownManager(DAOHub hub) {
        this.hub = hub;
    }

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

    public void start() {
        // Run every second (20 ticks)
        this.runTaskTimer(plugin, 0, 20);
    }

}
