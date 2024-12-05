package me.hektortm.woSSystems.systems.interactions.listeners;


import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.systems.interactions.config.InteractionConfig;
import me.hektortm.woSSystems.systems.interactions.InteractionManager;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.io.File;
import java.util.List;
import java.util.Map;

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

            if (block != null && !event.isCancelled()) {
                // Get the block's location as a string
                String locString = block.getWorld().getName() + "," + block.getX() + "," + block.getY() + "," + block.getZ();

                // Loop through all loaded interactions and check if the block is bound
                for (Map.Entry<String, InteractionConfig> entry : manager.getInteractionConfigs().entrySet()) {
                    InteractionConfig interaction = entry.getValue();

                    if (interaction.getBoundBlocks().contains(locString)) {
                        // Trigger the interaction
                        manager.triggerInteraction(interaction, event.getPlayer());
                        event.setCancelled(true); // Prevent further interaction with the block
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        // Check if the block is bound to an interaction
        String locString = block.getWorld().getName() + "," + block.getX() + "," + block.getY() + "," + block.getZ();

        for (InteractionConfig interaction : manager.getInteractionConfigs().values()) {
            if (interaction.getBoundBlocks().contains(locString)) {
                event.setCancelled(true); // Cancel block breaking
                event.getPlayer().sendMessage("You cannot break this block!");
                return;
            }
        }
    }


}
