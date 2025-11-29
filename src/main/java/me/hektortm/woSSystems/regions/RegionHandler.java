package me.hektortm.woSSystems.regions;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.internal.platform.WorldGuardPlatform;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.hektortm.woSSystems.WoSSystems;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Objects;

public class RegionHandler implements Listener {

    private final RegionBossBar bossbar;
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);

    public RegionHandler(RegionBossBar bossbar) {
        this.bossbar = bossbar;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        updateRegion(player);
    }

    public void updateRegion(Player player) {
        LocalPlayer localPlayer = WorldGuardPlugin.getPlugin(WorldGuardPlugin.class).wrapPlayer(player);
        RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(localPlayer.getWorld());

        if (regionManager == null) {
            bossbar.updateBossBar(player, ""); // Clear bossbar if no regions
            plugin.getPlayerRegions().remove(player.getUniqueId()); // Remove the player from tracked regions
            return;
        }

        BlockVector3 blockVector3 = localPlayer.getBlockLocation().toVector().toBlockPoint();
        ApplicableRegionSet regions = regionManager.getApplicableRegions(blockVector3);

        String newRegionId = null; // Track the region with a display-name flag

        for (ProtectedRegion region : regions) {
            String displayName = region.getFlag(WoSSystems.DISPLAY_NAME);
            String enterInteraction = region.getFlag(WoSSystems.ENTER_INTERACTION);
            String leaveInteraction = region.getFlag(WoSSystems.LEAVE_INTERACTION);
            newRegionId = region.getId();
            if (displayName != null) {
                bossbar.updateBossBar(player, displayName);

            }
            if (enterInteraction != null && !plugin.getPlayerRegions().containsValue(newRegionId)) {
                plugin.getInteractionManager().triggerInteraction(enterInteraction, player, null);

            }
        }

        String currentRegionId = plugin.getPlayerRegions().get(player.getUniqueId());

        // If the player has left their current region
        if (!Objects.equals(currentRegionId, newRegionId)) {
            if (newRegionId == null) {
                bossbar.updateBossBar(player, ""); // Clear the bossbar if no region with display-name
            }
             // Update the player's current region
        }
        plugin.getPlayerRegions().put(player.getUniqueId(), newRegionId);
    }

    public static String getRegionDisplayName(Player player) {
        LocalPlayer localPlayer = WorldGuardPlugin.getPlugin(WorldGuardPlugin.class).wrapPlayer(player);
        RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(localPlayer.getWorld());

        if (regionManager == null) {
            return "§6Unknown";
        }

        BlockVector3 blockVector3 = localPlayer.getBlockLocation().toVector().toBlockPoint();
        ApplicableRegionSet regions = regionManager.getApplicableRegions(blockVector3);
        String displayName = null;
        for (ProtectedRegion region : regions) {
            displayName = region.getFlag(WoSSystems.DISPLAY_NAME);

        }
        return displayName != null ? displayName : "§6Unknown";
    }
}
