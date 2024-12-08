package me.hektortm.woSSystems.systems.interactions.config;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class JSONLoader {
    private final File interactionFolder;
    private final File guiFolder;
    private Gson gson = new Gson();

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

    public InteractionConfig loadInteraction(File interactionFile) {
        if (!interactionFile.exists() || !interactionFile.getName().endsWith(".json")) {
            throw new IllegalArgumentException("Invalid interaction file: " + interactionFile.getName());
        }

        try (FileReader reader = new FileReader(interactionFile)) {
            return gson.fromJson(reader, InteractionConfig.class);
        } catch (IOException e) {
            System.err.println("Error reading interaction file: " + interactionFile.getName());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error parsing interaction file: " + interactionFile.getName());
            e.printStackTrace();
        }

        return null; // Return null if there was an error
    }
}