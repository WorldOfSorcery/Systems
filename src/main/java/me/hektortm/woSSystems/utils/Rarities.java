package me.hektortm.woSSystems.utils;

public enum Rarities {

    COMMON("Common",    Icons.COMMON.getIcon(),     50,  "§7"),
    UNCOMMON("Uncommon", Icons.UNCOMMON.getIcon(),  30,  "§a"),
    RARE("Rare",        Icons.RARE.getIcon(),        12,  "§b"),
    EPIC("Epic",        Icons.EPIC.getIcon(),         5,  "§5"),
    LEGENDARY("Legendary", Icons.LEGENDARY.getIcon(), 2, "§6"),
    ANCIENT("Ancient",  Icons.ANCIENT.getIcon(),    0.9,  "§c"),
    MYTHIC("Mythic",    Icons.MYTHIC.getIcon(),     0.1,  "§d");

    private final String name;
    private final String icon;
    private final double percentage;
    private final String color;

    Rarities(String name, String icon, double percentage, String color) {
        this.name = name;
        this.icon = icon;
        this.percentage = percentage;
        this.color = color;
    }

    public String getName() { return name; }
    public String getIcon() { return icon; }
    public double getPercentage() { return percentage; }
    public String getColor() { return color; }

    /** Returns the formatted lore line: §<color><icon> <name> */
    public String getLoreLine() {
        return "§f" + icon;
    }

    public static Rarities fromName(String name) {
        for (Rarities r : values()) {
            if (r.name.equalsIgnoreCase(name)) return r;
        }
        return null;
    }
}
