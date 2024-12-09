package me.hektortm.woSSystems.utils;

public enum Rarity {
    COMMON("common", 50),
    UNCOMMON("uncommon", 75),
    RARE("rare", 90),
    UNIQUE("unique", 98),
    LEGENDARY("legendary", 99),
    ANCIENT("ancient", 100);

    private final String name;
    private final int chanceThreshold;

    Rarity(String name, int chanceThreshold) {
        this.name = name;
        this.chanceThreshold = chanceThreshold;
    }

    public String getName() {
        return name;
    }

    public int getChanceThreshold() {
        return chanceThreshold;
    }

    /**
     * Determines the rarity based on a given chance.
     *
     * @param chance the chance value (0-100)
     * @return the matching Rarity
     */
    public static Rarity getRarityByChance(int chance) {
        for (Rarity rarity : values()) {
            if (chance < rarity.getChanceThreshold()) {
                return rarity;
            }
        }
        throw new IllegalArgumentException("Chance must be between 0 and 100");
    }
}
