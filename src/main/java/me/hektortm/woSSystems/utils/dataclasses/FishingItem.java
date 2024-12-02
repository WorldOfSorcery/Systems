package me.hektortm.woSSystems.utils.dataclasses;

public class FishingItem {

    private final String citem;
    private final String interaction;
    private final String rarity;

    public FishingItem(String citem, String interaction, String rarity) {
        this.citem = citem;
        this.interaction = interaction;
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

}
