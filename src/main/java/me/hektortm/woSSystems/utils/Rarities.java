package me.hektortm.woSSystems.utils;

public enum Rarities {

    COMMON("Common", Icons.COMMON.getIcon(), 50),
    UNCOMMON("Uncommon", Icons.UNCOMMON.getIcon(), 30),
    RARE("Rare", Icons.RARE.getIcon(), 12),
    EPIC("Epic", Icons.EPIC.getIcon(), 5),
    LEGENDARY("Legendary", Icons.LEGENDARY.getIcon(), 2),
    ANCIENT("Ancient", Icons.ANCIENT.getIcon(), 0.9),
    MYTHIC("Mythic", Icons.MYTHIC.getIcon(), 0.1);

    private final String name;
    private final String icon;
    private final double percentage;

    Rarities(String name, String icon, double percentage) {
        this.name = name;
        this.icon = icon;
        this.percentage = percentage;
    }

    public String getName() {
        return name;
    }

    public String getIcon() {
        return icon;
    }

    public double getPercentage() {
        return percentage;
    }

}
