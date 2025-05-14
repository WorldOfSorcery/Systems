package me.hektortm.woSSystems.systems.interactions;

import com.maximde.hologramlib.hologram.RenderMode;
import com.maximde.hologramlib.hologram.TextHologram;
import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.ConditionHandler;
import me.hektortm.woSSystems.utils.ConditionHandler_new;
import me.hektortm.woSSystems.utils.ConditionType;
import me.hektortm.woSSystems.utils.dataclasses.Condition;
import me.hektortm.woSSystems.utils.dataclasses.Interaction;
import me.hektortm.woSSystems.utils.dataclasses.InteractionAction;
import me.hektortm.woSSystems.utils.dataclasses.InteractionData;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Objects;

public class InteractionManager_new {

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
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Interaction inter : hub.getInteractionDAO().getInteractions()) {
                    for (Location location : inter.getBlockLocations()) {
                        if (location != null) {
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                particleHandler.spawnParticlesForPlayer(player, inter, location, false);

                                //spawnTextDisplay(location, inter, null, false);
                                //updateTextDisplay(location, inter, null, false);
                            }
                        }
                    }
                    for (int id : inter.getNpcIDs()) {
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            NPC npc1 = CitizensAPI.getNPCRegistry().getById(id);
                            Location location = npc1.getEntity().getLocation().getBlock().getLocation();
                            //particleHandler.spawnParticlesForPlayer(player, inter, npc1.getEntity().getLocation(), true);
                            //spawnTextDisplay(location, inter, id, true);
                            //updateTextDisplay(location, inter, id, true);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 10L);
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

    public void triggerInteraction(String interactionId, Player player) {
        Interaction inter = getInteraction(interactionId);


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
                } else { // default to "all"
                    shouldRun = conditions.checkConditions(player, conditionList);
                }

                if (!shouldRun) continue;
            }
            for (String cmd : action.getActions()) {
                String parsedCommand = cmd.replace("@p", player.getName());
                if (cmd.startsWith("send_message")) {
                    String message = cmd.replace("send_message", "");
                    player.sendMessage(message.replace("&", "§"));
                    return;
                }
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsedCommand);
            }

            if (action.getBehaviour().equalsIgnoreCase("break")) {
                break;
            }
        }

    }

}
