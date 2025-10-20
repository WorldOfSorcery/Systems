package me.hektortm.woSSystems.utils.dataclasses;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Loottable {
    private final String id;
    private final int amount;
    private final String name;
    private final List<LoottableItem> items;


    public Loottable(String id, int amount, String name, List<LoottableItem> items) {
        this.id = id;
        this.amount = amount;
        this.name = name;
        this.items = items;
    }

    public String getId() {
        return id;
    }
    public int getAmount() {
        return amount;
    }
    public String getName() {
        return name;
    }
    public List<LoottableItem> getItems() {
        return items;
    }

    public LoottableItem getRandom() {
        int total = getTotalWeight();
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
        // Should never happen if total > 0
        throw new IllegalStateException("No items in loot table");
    }

    public List<LoottableItem> getRandomItems() {
        return getRandomItems(this.amount);
    }

    public List<LoottableItem> getRandomItems(int count) {
        if (count <= 0) return Collections.emptyList();

        int total = getTotalWeight();
        if (total == 0) {
            throw new IllegalStateException("No items in loot table");
        }

        List<LoottableItem> rolls = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int r = ThreadLocalRandom.current().nextInt(total);
            int cumulative = 0;
            for (LoottableItem item : items) {
                int w = item.getWeight();
                if (w <= 0) continue;
                cumulative += w;
                if (r < cumulative) {
                    rolls.add(item);
                    break;
                }
            }
        }
        return rolls;
    }

    public int getTotalWeight() {
        int total = 0;
        for (LoottableItem item : items) {
            int w = item.getWeight();
            if (w > 0) total += w;
        }
        return total;
    }
}
