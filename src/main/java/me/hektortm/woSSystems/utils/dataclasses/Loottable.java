package me.hektortm.woSSystems.utils.dataclasses;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Loottable {
    private final String id;
    private final List<LoottableItem> items;


    public Loottable(String id, List<LoottableItem> items) {
        this.id = id;
        this.items = items;
    }

    public String getId() {
        return id;
    }
    public List<LoottableItem> getItems() {
        return items;
    }

    public LoottableItem getRandom() {
        int total = 0;
        for (LoottableItem item : items) {
            if (item.getWeight() > 0) total += item.getWeight();
        }
        if (total == 0) {
            throw new IllegalStateException("No items in loot table");
        }

        int r = ThreadLocalRandom.current().nextInt(total);

        int cumulative = 0;
        for (LoottableItem item : items) {
            int w = item.getWeight();
            if (w <= 0) continue;
            cumulative += w;
            if (r < cumulative) {
                return item;
            }
        }
        throw new IllegalStateException("No items in loot table");
    }

    public int getMaxWeight() {
        int maxWeight = 0;
        for (LoottableItem item : items) {
            item.getWeight();
            maxWeight += item.getWeight();
        }
        return maxWeight;
    }
}
