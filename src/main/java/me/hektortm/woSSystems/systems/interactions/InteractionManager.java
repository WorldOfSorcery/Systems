package me.hektortm.woSSystems.systems.interactions;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.ActionHandler;
import me.hektortm.woSSystems.utils.ConditionHandler;
import me.hektortm.woSSystems.utils.types.ConditionType;
import me.hektortm.woSSystems.utils.model.*;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

import static me.hektortm.woSSystems.systems.interactions.InterListener.buildKey;

/**
 * Central service for the interaction system.
 *
 * <p>Provides two main responsibilities:</p>
 * <ol>
 *   <li><b>Visual tick</b> ({@link #interactionTask()}) — a repeating
 *       {@link BukkitRunnable} that loads all interactions from the cache each
 *       second and spawns the configured particles and holograms around every
 *       block location and Citizens NPC that has an interaction bound to it.</li>
 *   <li><b>Interaction execution</b> ({@link #triggerInteraction}) — evaluates
 *       each {@link InteractionAction}'s conditions using {@link ConditionHandler}
 *       and, for every passing action group, dispatches the action list to
 *       {@link ActionHandler}.</li>
 * </ol>
 */
public class InteractionManager {

    private final DAOHub hub;
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final ConditionHandler conditions = plugin.getConditionHandler();
    private final ActionHandler actionHandler = plugin.getActionHandler();
    private final HologramManager hologramManager;

    /**
     * @param hub the DAO hub used to access interaction and condition data
     */
    public InteractionManager(DAOHub hub) {
        this.hub = hub;
        this.hologramManager = new HologramManager(hub);
    }

    /**
     * Returns the {@link HologramManager} used by this interaction manager.
     *
     * @return the hologram manager
     */
    public HologramManager getHologramManager() {
        return hologramManager;
    }

    private Interaction getInteraction(String id) {
        Interaction inter = hub.getInteractionDAO().getInteractionByID(id);
        return inter;
    }

    /**
     * Starts the repeating per-tick visual task for all interactions.
     *
     * <p>Every 20 ticks (once per second) the task loads the full interaction
     * cache asynchronously and then, back on the main thread, spawns particles
     * and manages holograms for each interaction's block locations and bound
     * NPCs for every online player.</p>
     */
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

    /**
     * Triggers an interaction for a player, evaluating conditions and
     * executing the resulting action lists.
     *
     * <p>Each {@link InteractionAction} in the interaction is checked against
     * its associated {@link Condition} list.  If the match type is
     * {@code "one"}, any passing condition is sufficient; otherwise all
     * conditions must pass.  Actions whose conditions fail are skipped.
     * Action groups with behaviour {@code "continue"} allow subsequent groups
     * to also run; any other behaviour value stops processing after the first
     * passing group.</p>
     *
     * @param interactionId the ID of the interaction to trigger
     * @param player        the player who triggered the interaction
     * @param key           the {@link InteractionKey} scoping local cooldowns
     *                      (may be {@code null} for global triggers)
     */
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
