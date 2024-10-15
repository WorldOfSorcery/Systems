package me.hektortm.woSSystems.utils;

public enum Permissions {

    CITEM_SAVE("citem.save"),
    CITEM_DELETE("citem.delete"),
    CITEM_UPDATE("citem.update"),
    CITEM_LORE("citem.lore"),
    CITEM_RENAME("citem.rename"),
    CITEM_FLAGS("citem.flags"),
    CITEM_ACTIONS("citem.actions"),
    CITEM_GIVE("citem.give"),
    CITEM_REMOVE("citem.remove"),

    INTER_BIND("interaction.bind"),
    INTER_UNBIND("interaction.unbind"),
    INTER_RELOAD("interaction.reload"),
    INTER_TRIGGER("interaction.trigger"),

    STATS_CREATE("stats.create"),
    STATS_DELETE("stats.delete"),
    STATS_GIVE("stats.give"),
    STATS_TAKE("stats.take"),
    STATS_SET("stats.set"),
    STATS_RESET("stats.reset"),
    STATS_RELOAD("stats.reload"),

    STATS_GLOBAL_CREATE("stats_global.create"),
    STATS_GLOBAL_DELETE("stats_global.delete"),
    STATS_GLOBAL_GIVE("stats_global.give"),
    STATS_GLOBAL_TAKE("stats_global.take"),
    STATS_GLOBAL_SET("stats_global.set"),
    STATS_GLOBAL_RESET("stats_global.reset"),
    STATS_GLOBAL_RELOAD("stats_global.reload"),

    UNLOCKABLE_CREATE("unlockable.create"),
    UNLOCKABLE_DELETE("unlockable.delete"),
    UNLOCKABLE_GIVE("unlockable.give"),
    UNLOCKABLE_TAKE("unlockable.take"),

    UNLOCKABLE_TEMP_CREATE("unlockable_temp.create"),
    UNLOCKABLE_TEMP_DELETE("unlockable_temp.delete"),
    UNLOCKABLE_TEMP_GIVE("unlockable_temp.give"),
    UNLOCKABLE_TEMP_TAKE("unlockable_temp.take");


    private final String permission;

    Permissions(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }

}
