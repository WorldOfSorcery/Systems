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

public class InteractionManager {

    private final DAOHub hub;
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final ConditionHandler conditions = plugin.getConditionHandler();
    private final ActionHandler actionHandler = plugin.getActionHandler();



    public InteractionManager(DAOHub hub) {
        this.hub = hub;
    }

    private Interaction getInteraction(String id) {
        Interaction inter = hub.getInteractionDAO().getInteractionByID(id);
        return inter;
    }

    public void interactionTask() {
        ParticleHandler particleHandler = new ParticleHandler(hub);
        //HologramHandler hologramHandler = new HologramHandler(hub);


        new BukkitRunnable() {
            @Override
            public void run() {
                for (Interaction inter : hub.getInteractionDAO().getInteractions()) {
                    for (Location location : inter.getBlockLocations()) {
                        if (location != null) {
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                particleHandler.spawnParticlesForPlayer(player, inter, location, false);

//                                Bukkit.getScheduler().runTask(plugin, () -> {
//                                    hologramHandler.handleHolograms(player, inter, location, false);
//                                });
                            }
                        }
                    }
                    for (int id : inter.getNpcIDs()) {
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            NPC npc1 = CitizensAPI.getNPCRegistry().getById(id);
                            if (npc1 == null || !npc1.isSpawned() || npc1.getEntity() == null) {
                                continue;
                            }
                            Location location = npc1.getEntity().getLocation().getBlock().getLocation();
                            particleHandler.spawnParticlesForPlayer(player, inter, npc1.getEntity().getLocation(), true);
                            //spawnTextDisplay(location, inter, id, true);
//                            Bukkit.getScheduler().runTask(plugin, () -> {
//                                hologramHandler.handleHolograms(player, inter, location, false);
//                            });
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 50L);
    }

    public void triggerInteraction(String interactionId, Player player) {
        Interaction inter = getInteraction(interactionId);
        if (inter == null) player.sendMessage("§cThis is not configured correctly. Please message a Staff member.");

        List<InteractionAction> actions = inter.getActions();

        for (InteractionAction action : actions) {
            List<Condition> conditionList = hub.getConditionDAO().getConditions(
                    ConditionType.INTERACTION,
                    interactionId + ":" + action.getActionId()
            );
            if (!conditionList.isEmpty()) {
                boolean shouldRun;
                if ("one".equalsIgnoreCase(action.getMatchType())) {
                    shouldRun = conditionList.isEmpty() || conditionList.stream().anyMatch(cond -> conditions.evaluate(player, cond));
                } else {
                    shouldRun = conditions.checkConditions(player, conditionList);
                }

                if (!shouldRun) continue;
            }

            Bukkit.getScheduler().runTask(WoSSystems.getPlugin(WoSSystems.class), () -> {actionHandler.executeActions(player, action.getActions(), ActionHandler.SourceType.INTERACTION, interactionId);});

            if (action.getBehaviour().equalsIgnoreCase("continue")) {
                continue;
            } else {
                break;
            }
        }

    }

}
