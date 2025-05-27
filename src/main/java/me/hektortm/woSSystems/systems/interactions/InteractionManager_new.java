package me.hektortm.woSSystems.systems.interactions;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.ConditionHandler;
import me.hektortm.woSSystems.utils.ConditionHandler_new;
import me.hektortm.woSSystems.utils.ConditionType;
import me.hektortm.woSSystems.utils.dataclasses.*;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

public class InteractionManager_new  {

    private final DAOHub hub;
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final ConditionHandler_new conditions = plugin.getConditionHandler_new();




    public InteractionManager_new(DAOHub hub) {
        this.hub = hub;
    }

    private Interaction getInteraction(String id) {
        Interaction inter = hub.getInteractionDAO().getInteractionByID(id);
        return inter;
    }



    public void interactionTask() {
        ParticleHandler particleHandler = new ParticleHandler(hub);
        HologramHandler hologramHandler = new HologramHandler(hub);


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

    public boolean interactionExist(String id) {
        Interaction inter = hub.getInteractionDAO().getInteractionByID(id);
        if (inter == null) {
            return false;
        }
        return true;
    }

    public void blockBind(String id, Location loc) {
        hub.getInteractionDAO().bindBlock(id, loc);
    }

    public void npcBind(String id, int npcId) {
        hub.getInteractionDAO().bindNPC(id, npcId);
    }

    public void triggerInteraction(String interactionId, Player player) {
        Interaction inter = getInteraction(interactionId);
        if (inter == null) return;

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
            for (String cmd : action.getActions()) {
                String parsedCommand = cmd.replace("@p", player.getName());
                if (cmd.startsWith("send_message")) {
                    String message = cmd.replace("send_message", "");
                    player.sendMessage(message.replace("&", "§"));
                }

                if(cmd.startsWith("eco")) {
                    String[] parts = cmd.split(" ");
                    String actionType = parts[1];
                    String currency = parts[3];
                    int amount = Integer.parseInt(parts[4]);
                    if (actionType.equalsIgnoreCase("give")) {
                        WoSSystems.getPlugin(WoSSystems.class).getEcoManager().ecoLog(player.getUniqueId(), currency, amount, "Interaction", inter.getInteractionId());

                    }
                    if (actionType.equalsIgnoreCase("take")) {
                        WoSSystems.getPlugin(WoSSystems.class).getEcoManager().ecoLog(player.getUniqueId(), currency, -amount, "Interaction", inter.getInteractionId());

                    }
                    if (actionType.equalsIgnoreCase("set")) {
                        WoSSystems.getPlugin(WoSSystems.class).getEcoManager().ecoLog(player.getUniqueId(), currency, amount, "Interaction", inter.getInteractionId());

                    }
                    if (actionType.equalsIgnoreCase("reset")) {
                        WoSSystems.getPlugin(WoSSystems.class).getEcoManager().ecoLog(player.getUniqueId(), currency, 0, "Interaction", inter.getInteractionId());

                    }
                }

                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsedCommand);
            }

            if (action.getBehaviour().equalsIgnoreCase("continue")) {
                continue;
            } else {
                break;
            }
        }

    }

}
