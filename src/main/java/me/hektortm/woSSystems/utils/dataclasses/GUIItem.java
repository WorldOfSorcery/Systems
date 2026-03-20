package me.hektortm.woSSystems.utils.dataclasses;

import org.bukkit.Material;

import java.util.List;

public class GUIItem {

    private final Material material;
    private final String display_name;
    private final List<String> lore;
    private final String model;
    private final String color;
    private final boolean enchanted;


    public GUIItem(Material material, String displayName, List<String> lore, String model, String color, boolean enchanted) {
        this.material = material;
        display_name = displayName;
        this.lore = lore;
        this.model = model;
        this.color = color;
        this.enchanted = enchanted;
    }

    public Material getMaterial() {
        return material;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public List<String> getLore() {
        return lore;
    }

    public String getModel() {
        return model;
    }

    public String getColor() {
        return color;
    }

    public boolean isEnchanted() {
        return enchanted;
    }
}
