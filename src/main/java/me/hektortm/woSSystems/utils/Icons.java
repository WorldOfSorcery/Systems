package me.hektortm.woSSystems.utils;

import org.bukkit.Bukkit;

public enum Icons {

    MYTHIC("\uE000"),
    ANCIENT("\uE001"),
    LEGENDARY("\uE002"),
    EPIC("\uE003"),
    RARE("\uE004"),
    UNCOMMON("\uE005"),
    COMMON("\uE006"),

    INGREDIENT("\uE100"),
    COLLECTABLE("\uE101"),
    POTION("\uE102"),
    BOUND("\uE103"),
    FOOD("\uE104"),
    QUEST("\uE105"),
    WEARABLE("\uE106"),
    MAGIC("\uE107"),
    SIGNED_BY("\uE108"),
    TIME("\uE109"),

    MAGIC_CHARM("\uE200"),
    MAGIC_HEX("\uE201"),
    MAGIC_CURSE("\uE202"),
    MAGIC_DARK("\uE203"),
    MAGIC_SINISTER("\uE204"),
    MAGIC_UTILITY("\uE205"),
    MAGIC_DEFENSE("\uE206"),
    MAGIC_COMBAT("\uE207"),

    RANK_HEADSTAFF("\uE300"),
    RANK_SR("\uE301"),
    RANK_STAFF("\uE302"),
    RANK_JR("\uE303"),
    RANK_DEV("\uE304"),
    RANK_MOD("\uE305"),

    BANNER("\uE600");




    Icons(String icon) {
        this.icon = icon;
    }

    private final String icon;

    public String getIcon() {
        return icon;
    }

    public static String getIconByName(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }

        String trimmedName = name.trim();

        for (Icons icon : Icons.values()) {
            if (icon.name().equalsIgnoreCase(trimmedName)) {
                return icon.getIcon();
            }
        }

        return null;
    }

}
