package me.hektortm.woSSystems.systems.cooldowns;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.dataclasses.Cooldown;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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
        try {
            // Get all active cooldowns from database
            Map<UUID, List<String>> activeCooldowns = hub.getCooldownDAO().getAllActiveCooldowns();

            // Process each player's cooldowns
            for (Map.Entry<UUID, List<String>> entry : activeCooldowns.entrySet()) {
                UUID playerId = entry.getKey();
                OfflinePlayer oP = Bukkit.getOfflinePlayer(playerId);
                plugin.getLogger().info("Processing cooldowns for player: " + oP.getName() + " (UUID: " + playerId + ")");

                // Check each cooldown
                for (String cooldownId : entry.getValue()) {
                    if (hub.getCooldownDAO().isCooldownExpired(oP, cooldownId)) {
                        // Remove expired cooldown
                        plugin.getLogger().info("Cooldown expired for player: " + oP.getName() + " with ID: " + cooldownId);
                        hub.getCooldownDAO().removeCooldown(oP, cooldownId);

                        // Trigger completion action
                        onCooldownExpire(oP, cooldownId);
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error processing cooldowns: " + e.getMessage());
        }
    }

    private void onCooldownExpire(OfflinePlayer player, String cooldownId) {
        // Customize these actions based on your cooldown types
        plugin.writeLog("CooldownManager", Level.INFO, "Cooldown expired for player: " + player.getName() + " with ID: " + cooldownId);
        Cooldown cd = hub.getCooldownDAO().getCooldownByID(cooldownId);
        String endInt = cd.getEnd_interaction();

        if (endInt != null) plugin.getInteractionManager().triggerInteraction(endInt, player.getPlayer());

    }

    public void start() {
        // Run every second (20 ticks)
        this.runTaskTimer(plugin, 0, 20);
    }

}
