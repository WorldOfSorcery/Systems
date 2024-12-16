package me.hektortm.woSSystems.utils;

public enum Icons {

    LEGENDARY(""),
    DIVINE(""),
    ANCIENT(""),
    EPIC(""),
    RARE(""),
    UNCOMMON(""),
    COMMON(""),

    INGREDIENT(""),
    COLLECTABLE(""),
    POTION(""),
    BOUND(""),
    FOOD(""),
    QUEST(""),
    WEARABLE(""),

    DEV(""),
    HEAD_OF_DEV("");




    Icons(String icon) {
        this.icon = icon;
    }

    private final String icon;

    public String getIcon() {
        return icon;
    }

}
