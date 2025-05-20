package me.hektortm.woSSystems.professions.fishing;

import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.database.dao.FishingDAO;
import me.hektortm.woSSystems.utils.dataclasses.FishingItem;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FishingManager {

    private final DAOHub hub;


    public FishingManager(DAOHub hub) {
        this.hub = hub;
    }

    public List<FishingItem> getItemByRarity(String rarity) {
        return fishingItemsByRarity.getOrDefault(rarity, new ArrayList<>());
    }

    public FishingItem getRandomItem(String rarity) {
        List<FishingItem> items = getItemByRarity(rarity);
        if (items.isEmpty()) return null;
        return items.get((int) (Math.random() * items.size()));
    }

    public FishingDAO getDAO() {
        return hub.getFishingDAO();
    }

}
