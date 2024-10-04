package me.hektortm.woSSystems.interactions.listeners;


import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.interactions.core.InteractionConfig;
import me.hektortm.woSSystems.interactions.core.InteractionManager;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.io.File;
import java.util.List;

public class InterBlockListener implements Listener {

    private final WoSSystems plugin;
    private final InteractionManager manager;

    public InterBlockListener(WoSSystems plugin, InteractionManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if (block != null) {
                String locString = block.getWorld().getName() + "," + block.getX() + "," + block.getY() + "," + block.getZ();

                // Iterate through all interaction files to check if the block is bound to any interaction
                File folder = new File(plugin.getDataFolder() + "/bound/");
                File[] files = folder.listFiles();
                if (files != null) {
                    for (File file : files) {
                        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                        List<String> blocks = config.getStringList("block");

                        if (blocks.contains(locString)) {
                            String interactionID = file.getName().replace(".yml", "");
                            InteractionConfig interaction = manager.getInteractionById(interactionID);
                            manager.triggerInteraction(interaction, event.getPlayer());  // Trigger the interaction for the player
                            event.setCancelled(true);  // Prevent breaking or interacting with the block
                            return;
                        }
                    }
                }
            }
        }
    }


    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.hasMetadata("unbreakable")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("You cannot break this block!");
        }
    }


}
