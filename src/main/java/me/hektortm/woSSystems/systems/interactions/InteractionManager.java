package me.hektortm.woSSystems.systems.interactions;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.ActionHandler;
import me.hektortm.woSSystems.utils.ConditionHandler;
import me.hektortm.woSSystems.utils.ConditionType;
import me.hektortm.woSSystems.utils.dataclasses.*;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.logging.Logger;

import static me.hektortm.woSSystems.listeners.InterListener.buildKey;

public class InteractionManager {

    private final DAOHub hub;
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final ConditionHandler conditions = plugin.getConditionHandler();
    private final ActionHandler actionHandler = plugin.getActionHandler();
    private final HologramManager hologramManager;

    public InteractionManager(DAOHub hub) {
        this.hub = hub;
        this.hologramManager = new HologramManager(hub);
    }

    public HologramManager getHologramManager() {
        return hologramManager;
    }

    private Interaction getInteraction(String id) {
        Interaction inter = hub.getInteractionDAO().getInteractionByID(id);
        return inter;
    }

    public void interactionTask() {
        ParticleHandler particleHandler = new ParticleHandler(hub);
        plugin.getLogger().info("[InteractionManager] Starting interaction task.");

        new BukkitRunnable() {
            @Override
            public void run() {
                // Load interaction data off the main thread, then process visuals on main thread
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    List<Interaction> interactions = hub.getInteractionDAO().cache();
                    plugin.getLogger().fine("[InteractionManager] Tick — loaded " + interactions.size() + " interaction(s).");
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        for (Interaction inter : interactions) {
                            for (Location location : inter.getBlockLocations()) {
                                if (location != null) {
                                    for (Player player : Bukkit.getOnlinePlayers()) {
                                        InteractionKey key = buildKey(location);
                                        particleHandler.spawnParticlesForPlayer(player, inter, location, false, key);
                                        hologramManager.handleHolograms(player, inter, location, false, key);
                                    }
                                }
                            }
                            for (int id : inter.getNpcIDs()) {
                                for (Player player : Bukkit.getOnlinePlayers()) {
                                    NPC npc1 = CitizensAPI.getNPCRegistry().getById(id);
                                    if (npc1 == null || !npc1.isSpawned() || npc1.getEntity() == null) {
                                        continue;
                                    }
                                    Location location = npc1.getEntity().getLocation();
                                    InteractionKey key = new InteractionKey("npc:" + id);
                                    particleHandler.spawnParticlesForPlayer(player, inter, location, true, key);
                                    hologramManager.handleHolograms(player, inter, location, true, key, npc1.getEntity().getHeight());
                                }
                            }
                        }
                    });
                });
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void triggerInteraction(String interactionId, Player player, InteractionKey key) {
        Interaction inter = getInteraction(interactionId);
        if (inter == null) {
            player.sendMessage("§cThis is not configured correctly. Please message a Staff member.");
            return;
        }

        List<InteractionAction> actions = inter.getActions();

        for (InteractionAction action : actions) {
            List<Condition> conditionList = hub.getConditionDAO().getConditions(
                    ConditionType.INTERACTION,
                    interactionId + ":" + action.getActionId()
            );
            if (!conditionList.isEmpty()) {
                boolean shouldRun;
                if ("one".equalsIgnoreCase(action.getMatchType())) {
                    shouldRun = conditionList.isEmpty() || conditionList.stream().anyMatch(cond -> conditions.evaluate(player, cond, key));
                } else {
                    shouldRun = conditions.checkConditions(player, conditionList, key);
                }

                if (!shouldRun) continue;
            }

            Bukkit.getScheduler().runTask(WoSSystems.getPlugin(WoSSystems.class), () -> {actionHandler.executeActions(player, action.getActions(), ActionHandler.SourceType.INTERACTION, interactionId, key);});

            if (action.getBehaviour().equalsIgnoreCase("continue")) {
                continue;
            } else {
                break;
            }
        }

    }

}
