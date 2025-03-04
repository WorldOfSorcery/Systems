package me.hektortm.woSSystems.utils.dataclasses;

import java.util.List;

public class FishingItem {

    private final String id;
    private final String citem;
    private final String interaction;
    private final List<String> regions;
    private final String rarity;

    public FishingItem(String id, String citem, String interaction, List<String> regions, String rarity) {
        this.id = id;
        this.citem = citem;
        this.interaction = interaction;
        this.regions = regions;
        this.rarity = rarity;
    }


    public String getCitem() {
        return citem;
    }
    public String getInteraction() {
        return interaction;
    }
    public String getRarity() {
        return rarity;
    }


    public List<String> getRegions() {
        return regions;
    }

    public String getId() {
        return id;
    }
}
