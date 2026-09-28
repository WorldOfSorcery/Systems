package me.hektortm.woSSystems.systems.quests;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.systems.quests.model.PlayerQuestState;
import me.hektortm.woSSystems.systems.quests.model.Quest;
import me.hektortm.woSSystems.systems.quests.model.QuestNode;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.aselstudios.luxdialoguesapi.Builders.Dialogue;
import org.aselstudios.luxdialoguesapi.Events.DialogueStartEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;

/**
 * Listens for Bukkit events and advances matching quest objectives.
 *
 * <p>Objective types handled:
 * <ul>
 *   <li>{@code kill}     — {@link EntityDeathEvent}</li>
 *   <li>{@code collect}  — {@link EntityPickupItemEvent}</li>
 *   <li>{@code craft}    — {@link CraftItemEvent}</li>
 *   <li>{@code interact} — {@link PlayerInteractEntityEvent}</li>
 *   <li>{@code reach}    — {@link PlayerMoveEvent} (WorldGuard region check)</li>
 *   <li>{@code custom}   — triggered externally via {@link QuestManager#progressObjective}</li>
 * </ul>
 */
public class QuestListener implements Listener {

    private final WoSSystems plugin;
    private final QuestManager questManager;

    public QuestListener(WoSSystems plugin, QuestManager questManager) {
        this.plugin       = plugin;
        this.questManager = questManager;
    }

    // -------------------------------------------------------------------------
    // Player lifecycle
    // -------------------------------------------------------------------------

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();
        org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(plugin,
                () -> questManager.onPlayerJoin(uuid));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        questManager.onPlayerQuit(event.getPlayer().getUniqueId());
    }

    // -------------------------------------------------------------------------
    // Kill objectives
    // -------------------------------------------------------------------------

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();
        if (killer == null) return;

        checkObjectives(killer, "kill",
                entity.getType().name().toLowerCase(),
                getEntityName(entity));
    }

    // -------------------------------------------------------------------------
    // Collect objectives
    // -------------------------------------------------------------------------

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        ItemStack item = event.getItem().getItemStack();
        checkObjectives(player, "collect",
                item.getType().name().toLowerCase(),
                getItemName(item));
    }

    // -------------------------------------------------------------------------
    // Craft objectives
    // -------------------------------------------------------------------------

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack result = event.getRecipe().getResult();
        checkObjectives(player, "craft",
                result.getType().name().toLowerCase(),
                getItemName(result));
    }

    // -------------------------------------------------------------------------
    // Interact objectives  (right-click on NPC / entity)
    // -------------------------------------------------------------------------

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        LivingEntity entity = (event.getRightClicked() instanceof LivingEntity le) ? le : null;
        if (entity == null) return;

        checkObjectives(player, "interact",
                entity.getType().name().toLowerCase(),
                getEntityName(entity));
    }

    // -------------------------------------------------------------------------
    // Interact objectives  (right-click on NPC / entity)
    // -------------------------------------------------------------------------

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDialogStart(DialogueStartEvent event) {
        Player p = event.getPlayer();
        Dialogue d = event.getDialogue();

        checkObjectives(p, "talk", d.getDialogueID().toLowerCase(), null);
    }


    // -------------------------------------------------------------------------
    // Reach objectives  (WorldGuard region entry)
    // -------------------------------------------------------------------------

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        // Only react when the player crosses a block boundary to limit overhead
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;

        Player player = event.getPlayer();
        Collection<PlayerQuestState> states = questManager.getActiveStates(player.getUniqueId());
        if (states.isEmpty()) return;

        // Build the full set of applicable region IDs at the player's current location,
        // including parent regions, so child/sub-regions are matched correctly.
        Set<String> applicableRegions = getApplicableRegionIds(player);

        for (PlayerQuestState state : states) {
            Quest quest = questManager.getQuest(state.getQuestId());
            if (quest == null) continue;

            for (String nodeId : new ArrayList<>(state.getActiveNodes())) {
                QuestNode node = quest.getNode(nodeId);
                if (node == null || !"objective".equals(node.getType())) continue;
                if (!"reach".equals(node.getObjectiveType())) continue;

                String targetId = node.getObjectiveTargetId();
                if (targetId == null) continue;

                // Match against any applicable region ID (case-insensitive) or world name
                boolean inTarget = applicableRegions.stream()
                        .anyMatch(r -> r.equalsIgnoreCase(targetId))
                        || player.getWorld().getName().equalsIgnoreCase(targetId);

                if (inTarget) questManager.progressObjective(player, state.getQuestId(), nodeId, 1);
            }
        }
    }

    /**
     * Returns the IDs of every WorldGuard region applicable at the player's current
     * location — including parent (super) regions of each matched region.
     * This ensures that targeting a parent region like "hogwarts" also matches when
     * the player is inside a child region like "classroom".
     */
    private Set<String> getApplicableRegionIds(Player player) {
        Set<String> ids = new HashSet<>();
        try {
            var localPlayer = WorldGuardPlugin.getPlugin(WorldGuardPlugin.class).wrapPlayer(player);
            RegionManager rm = WorldGuard.getInstance().getPlatform()
                    .getRegionContainer().get(localPlayer.getWorld());
            if (rm == null) return ids;

            BlockVector3 pos = localPlayer.getBlockLocation().toVector().toBlockPoint();
            ApplicableRegionSet regionSet = rm.getApplicableRegions(pos);

            for (ProtectedRegion region : regionSet) {
                // Add the region itself
                ids.add(region.getId());
                // Walk up the parent chain so "hogwarts" is included when in "classroom"
                ProtectedRegion parent = region.getParent();
                while (parent != null) {
                    ids.add(parent.getId());
                    parent = parent.getParent();
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("QuestListener: WorldGuard region lookup failed: " + e.getMessage());
        }
        return ids;
    }

    // -------------------------------------------------------------------------
    // Shared objective matching
    // -------------------------------------------------------------------------

    private void checkObjectives(Player player, String type, String typeName, String displayName) {
        Collection<PlayerQuestState> states = questManager.getActiveStates(player.getUniqueId());
        if (states.isEmpty()) return;

        for (PlayerQuestState state : states) {
            Quest quest = questManager.getQuest(state.getQuestId());
            if (quest == null) continue;

            for (String nodeId : new ArrayList<>(state.getActiveNodes())) {
                QuestNode node = quest.getNode(nodeId);
                if (node == null || !"objective".equals(node.getType())) continue;
                if (!type.equals(node.getObjectiveType())) continue;

                if (matchesTarget(node.getObjectiveTargetId(), typeName, displayName)) {
                    questManager.progressObjective(player, state.getQuestId(), nodeId, 1);
                }
            }
        }
    }

    /**
     * Matches a target ID against the entity/item type name or display name.
     * A {@code null} or empty targetId matches anything.
     */
    // Package-private static for unit testing (pure String logic, no instance state). See QuestListenerMatchTest.
    static boolean matchesTarget(String targetId, String typeName, String displayName) {
        if (targetId == null || targetId.isEmpty()) return true;
        String t = targetId.toLowerCase();
        if (t.equals(typeName)) return true;
        if (displayName != null) {
            String norm = displayName.toLowerCase().replace(" ", "_");
            if (t.equals(norm) || t.equals(displayName.toLowerCase())) return true;
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // Name helpers
    // -------------------------------------------------------------------------

    private String getEntityName(LivingEntity entity) {
        if (entity.customName() != null)
            return PlainTextComponentSerializer.plainText().serialize(entity.customName());
        return null;
    }

    private String getItemName(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasDisplayName() && meta.displayName() != null)
            return PlainTextComponentSerializer.plainText().serialize(meta.displayName());
        return null;
    }
}
