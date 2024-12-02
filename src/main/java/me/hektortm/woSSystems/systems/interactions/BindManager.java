package me.hektortm.woSSystems.systems.interactions;


import me.hektortm.woSSystems.WoSSystems;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class BindManager {

    private final WoSSystems plugin;

    public BindManager(WoSSystems plugin) {
        this.plugin = plugin;
    }

    public void bindInteractionToBlock(String interactionID, Location blockLocation) {
        File file = new File(plugin.getDataFolder() + "/bound/", interactionID + ".yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        // Add the block location to the config
        String locString = blockLocation.getWorld().getName() + "," + blockLocation.getBlockX() + "," +
                blockLocation.getBlockY() + "," + blockLocation.getBlockZ();

        List<String> blocks = config.getStringList("block");
        if (!blocks.contains(locString)) {
            blocks.add(locString);
            config.set("block", blocks);
        }

        // Save the config back to the file
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Make the block unbreakable
        blockLocation.getBlock().setMetadata("unbreakable", new FixedMetadataValue(plugin, true));
    }

    public void unbindInteractionFromBlock(String interactionID, Location blockLocation) {
        File file = new File(plugin.getDataFolder() + "/bound/", interactionID + ".yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        String locString = blockLocation.getWorld().getName() + "," + blockLocation.getBlockX() + "," +
                blockLocation.getBlockY() + "," + blockLocation.getBlockZ();

        List<String> blocks = config.getStringList("block");

        // Remove the block location if it exists
        if (blocks.contains(locString)) {
            blocks.remove(locString);
            config.set("block", blocks);

            // Save the updated config
            try {
                config.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Optionally, remove the "unbreakable" metadata from the block
            blockLocation.getBlock().removeMetadata("unbreakable", plugin);
        } else {
            System.out.println("This block is not bound to any interaction.");
        }
    }

    public Location getTargetBlockLocation(Player player) {

        double maxDistance = 5.0;


        Block targetBlock = player.getTargetBlock(null, (int) maxDistance);

        if (targetBlock == null) {
            return null;
        }

        return targetBlock.getLocation();
    }


}
