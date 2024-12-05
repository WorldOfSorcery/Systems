package me.hektortm.woSSystems.systems.interactions.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JSONLoader {
    private final File interactionFolder;
    private final File guiFolder;

    public JSONLoader(File pluginDataFolder) {
        interactionFolder = new File(pluginDataFolder, "interactions");
        if (!interactionFolder.exists()) {
            interactionFolder.mkdirs();
        }

        guiFolder = new File(pluginDataFolder, "guis");
        if (!guiFolder.exists()) {
            guiFolder.mkdirs();
        }
    }

    public Map<String, InteractionConfig> loadInteractions() {
        return loadConfigs(interactionFolder, InteractionConfig::new);
    }

    public Map<String, GUIConfig> loadGUIs() {
        return loadConfigs(guiFolder, GUIConfig::new);
    }

    private <T> Map<String, T> loadConfigs(File folder, ConfigParser<T> parser) {
        Map<String, T> configs = new HashMap<>();
        for (File file : folder.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".json")) {
                try (FileReader reader = new FileReader(file)) {
                    JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
                    String id = file.getName().replace(".json", "");
                    configs.put(id, parser.parse(jsonObject));
                } catch (IOException e) {
                    System.err.println("Failed to load config file: " + file.getName());
                    e.printStackTrace();
                }
            }
        }
        return configs;
    }

    @FunctionalInterface
    interface ConfigParser<T> {
        T parse(JsonObject jsonObject);
    }
}
