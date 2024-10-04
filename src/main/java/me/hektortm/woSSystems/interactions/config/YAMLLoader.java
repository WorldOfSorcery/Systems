package me.hektortm.woSSystems.interactions.config;


import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.interactions.core.InteractionConfig;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class YAMLLoader {

    private Plugin plugin;
    private final File interactionFolder;

    public YAMLLoader(Plugin plugin) {
        this.plugin = plugin;
        interactionFolder = new File(plugin.getDataFolder(), "interactions");
        if (!interactionFolder.exists()) {
            interactionFolder.mkdirs();
        }
    }

    // Load all interactions
    public Map<String, InteractionConfig> loadInteractions() {
        Map<String, InteractionConfig> interactions = new HashMap<>();


        for (File file : interactionFolder.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".yml")) {
                String id = file.getName().replace(".yml", "");
                File interactionFile = new File(WoSSystems.getPlugin(WoSSystems.class).getDataFolder(), "interactions/" + id + ".yml");
                InteractionConfig config = loadInteraction(interactionFile);
                if (config != null) {
                    interactions.put(id, config);
                }
            }
        }

        return interactions;
    }

    // Load single interaction
    public InteractionConfig loadInteraction(File file) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        // Ensure you're using getConfigurationSection instead of casting
        return new InteractionConfig(config);
    }

}