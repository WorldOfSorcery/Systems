package me.hektortm.woSSystems.professions.fishing;

import me.hektortm.woSSystems.professions.utils.FishingItem;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FishingManager {

    private final Map<String, List<FishingItem>> fishingItemsByRarity = new HashMap<>();

    public FishingManager(File itemFolder) {
        loadFishingItems(itemFolder);
    }

    private void loadFishingItems(File folder) {
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);

                String citem = config.getString("citem");
                String interaction = config.getString("interaction");
                String rarity = config.getString("rarity");

                FishingItem fishingItem = new FishingItem(citem, interaction, rarity);

                fishingItemsByRarity.computeIfAbsent(rarity, k -> new ArrayList<>()).add(fishingItem);

            }
        }

    }

    public List<FishingItem> getItemByRarity(String rarity) {
        return fishingItemsByRarity.getOrDefault(rarity, new ArrayList<>());
    }

    public FishingItem getRandomItem(String rarity) {
        List<FishingItem> items = getItemByRarity(rarity);
        if (items.isEmpty()) return null;
        return items.get((int) (Math.random() * items.size()));
    }


}
