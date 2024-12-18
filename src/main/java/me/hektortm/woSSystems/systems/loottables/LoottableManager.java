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
    public final Map<String, Map<String, Integer>> loottableMap = new HashMap<>();

    public LoottableManager(InteractionManager interManager, CitemManager citemManager) {
        this.interManager = interManager;
        this.citemManager = citemManager;

        if (!loottableFolder.exists()) {
            loottableFolder.mkdir();
        }

        loadLoottables();
    }

    public void loadLoottables() {
        File[] loottableFiles = loottableFolder.listFiles((dir, name) -> name.endsWith(".json"));
        if (loottableFiles == null || loottableFiles.length == 0) {
            log.sendWarning("No valid interactions loaded. Check JSON structure in " + loottableFolder.getPath());
            return;
        }

        for (File file : loottableFiles) {
            String id = file.getName().replace(".json", "");
            try (FileReader reader = new FileReader(file)) {
                JSONObject json = (JSONObject) new JSONParser().parse(reader);

                Map<String, Integer> lootTable = new HashMap<>();
                for (Object key : json.keySet()) {
                    String action = (String) key;
                    int chance = ((Long) json.get(key)).intValue();
                    lootTable.put(action, chance);
                }

                loottableMap.put(id, lootTable);
            } catch (Exception e) {
                Bukkit.getLogger().warning("Error loading loot tables from file " + file.getName());
            }

        }
    }



    public void giveLoottables(Player p, String id) {
        Map<String, Integer> lootTable = loottableMap.get(id);

        if (lootTable == null || lootTable.isEmpty()) {
            Bukkit.getLogger().warning("Loot table with ID '"+id+"' not found or is empty");
            return;
        }

        int totalWeight = lootTable.values().stream().mapToInt(Integer::intValue).sum();

        if (totalWeight <= 0) {
            Bukkit.getLogger().warning("Loot table with ID '" + id + "' has no valid chances.");
            return;
        }

        int randomValue = new Random().nextInt(totalWeight);

        int currentWeight = 0;
        for(Map.Entry<String, Integer> entry : lootTable.entrySet()) {
            currentWeight += entry.getValue();
            if (randomValue < currentWeight) {
                String action = entry.getKey();



                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), action.replace("%player%", p.getName()));
                return;

            }
        }
        Bukkit.getLogger().warning("No action executed for loot table with ID '"+id+"'");

    }




}
