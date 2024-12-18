package me.hektortm.woSSystems.systems.loottables;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.systems.citems.CitemManager;
import me.hektortm.woSSystems.systems.interactions.InteractionManager;
import me.hektortm.wosCore.LangManager;
import me.hektortm.wosCore.WoSCore;
import me.hektortm.wosCore.logging.LogManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class LoottableManager {

    private final InteractionManager interManager;
    private final CitemManager citemManager;
    private final WoSSystems woSSystems = WoSSystems.getPlugin(WoSSystems.class);
    private final WoSCore core = WoSCore.getPlugin(WoSCore.class);
    private final LogManager log = new LogManager(new LangManager(core), core);
    private final File loottableFolder = new File(woSSystems.getDataFolder(), "loottables");
    public final Map<String, JSONObject> loottableMap = new HashMap<>();

    public LoottableManager(InteractionManager interManager, CitemManager citemManager) {
        this.interManager = interManager;
        this.citemManager = citemManager;

        if (!loottableFolder.exists()) {
            loottableFolder.mkdir();
        }

        loadLoottables();
    }

    public void loadLoottables() {
        File[] lootTableFiles = loottableFolder.listFiles((dir, name) -> name.endsWith(".json"));
        if (lootTableFiles == null || lootTableFiles.length == 0) {
            Bukkit.getLogger().warning("No valid loot tables found in folder: " + loottableFolder.getPath());
            return;
        }

        for (File file : lootTableFiles) {
            String lootTableId = file.getName().replace(".json", "");
            try (FileReader reader = new FileReader(file)) {
                JSONObject lootTableJson = (JSONObject) new JSONParser().parse(reader);

                // Validate and store the loot table
                loottableMap.put(lootTableId, lootTableJson);

                Bukkit.getLogger().info("Loaded loot table: " + lootTableId);

            } catch (Exception e) {
                Bukkit.getLogger().warning("Error loading loot table from file " + file.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }



    public void giveLoottables(Player p, String id) {
        JSONObject lootTable = loottableMap.get(id);

        if (lootTable == null || lootTable.isEmpty()) {
            Bukkit.getLogger().warning("Loot table with ID '"+id+"' not found or is empty");

            return;
        }

        Map<String, Integer> weightedActions = new HashMap<>();
        lootTable.forEach((category, value) -> {
            JSONObject categoryItems = (JSONObject) value;
            categoryItems.forEach((action, weight) -> {
                weightedActions.put(category + ":" + action, ((Long) weight).intValue());
            });
        });

        int totalWeight = weightedActions.values().stream().mapToInt(Integer::intValue).sum();

        if (totalWeight <= 0) {
            Bukkit.getLogger().warning("Loot table with ID '" + id + "' has no valid chances.");
            return;
        }

        // Generate a random value
        int randomValue = new Random().nextInt(totalWeight);

        // Determine which action to execute
        int currentWeight = 0;
        for (Map.Entry<String, Integer> entry : weightedActions.entrySet()) {
            currentWeight += entry.getValue();
            if (randomValue < currentWeight) {
                String[] parts = entry.getKey().split(":");
                String category = parts[0];
                String action = parts[1];

                switch (category) {
                    case "command":
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), action.replace("{player}", p.getName()));
                        break;
                    case "citem":
                        citemManager.giveCitem(Bukkit.getConsoleSender(), p, action, 1);
                        break;
                    case "interaction":
                        interManager.triggerInteraction(p, action);
                        break;
                    default:
                        Bukkit.getLogger().warning("Unrecognized category: " + category);
                        break;
                }
                return; // Exit after executing one action
            }
        }
    }




}
