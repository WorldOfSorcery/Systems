package me.hektortm.woSSystems.utils.dataclasses;

import org.bukkit.Material;

import java.util.List;

public class GUISlot {

    private final String guiId;
    private final int slot;
    private final int slot_id;
    private final String matchType;
    private final Material material;
    private final String displayName;
    private final List<String> lore;
    private final int customModelData;
    private final boolean enchanted;
    private final List<String> right_actions;
    private final List<String> left_actions;
    private final boolean visible;


    public GUISlot(String guiId, int slot, int slot_id, String matchType, Material material, String displayName, List<String> lore, int customModelData, boolean enchanted, List<String> rightActions, List<String> leftActions, boolean visible) {
        this.guiId = guiId;
        this.slot = slot;
        this.slot_id = slot_id;
        this.matchType = matchType;
        this.material = material;
        this.displayName = displayName;
        this.lore = lore;
        this.customModelData = customModelData;
        this.enchanted = enchanted;
        right_actions = rightActions;
        left_actions = leftActions;
        this.visible = visible;
    }

    public String getGuiId() {
        return guiId;
    }
    public int getSlot() {
        return slot;
    }
    public int getSlotId() {
        return slot_id;
    }
    public String getMatchType() {
        return matchType;
    }
    public Material getMaterial() {
        return material;
    }
    public List<String> getRight_actions() {
        return right_actions;
    }
    public List<String> getLeft_actions() {
        return left_actions;
    }
    public boolean isVisible() {
        return visible;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getLore() {
        return lore;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public boolean isEnchanted() {
        return enchanted;
    }
}
