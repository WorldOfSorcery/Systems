package me.hektortm.woSSystems.utils.dataclasses;

import org.bukkit.Material;

import java.util.List;

public class GUIItem {
    private final Material material;
    private final int slot;
    private final String displayName;
    private final List<String> lore;
    private final String action;

    public GUIItem(Material material, int slot, String displayName, List<String> lore, String action) {
        this.material = material;
        this.slot = slot;
        this.displayName = displayName;
        this.lore = lore;
        this.action = action;
    }

    public Material getMaterial() {
        return material;
    }

    public int getSlot() {
        return slot;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getLore() {
        return lore;
    }

    public String getAction() {
        return action;
    }
}