package me.hektortm.woSSystems.utils;

public enum Permissions {

    CITEM_SAVE("citem.save"),
    CITEM_DELETE("citem.delete"),
    CITEM_UPDATE("citem.update"),
    CITEM_LORE("citem.modify.lore"),
    CITEM_RENAME("citem.modify.rename"),
    CITEM_FLAGS("citem.modify.flags"),
    CITEM_ACTIONS("citem.modify.actions"),
    CITEM_GIVE("citem.give"),
    CITEM_REMOVE("citem.remove"),
    CITEM_TAG("citem.tag"),
    CITEM_INFO("citem.info"),
    CITEM_MODEL("citem.modify.model"),
    CITEM_COLOR("citem.modify.color"),
    CITEM_TOOLTIP("citem.modify.tooltip"),
    CITEM_SIGN("citem.sign"),

    CRECIPE_CREATE("crecipe.create"),
    CRECIPE_DELETE("crecipe.delete"),
    CRECIPE_RELOAD("crecipe.reload"),

    INTER_BIND("interaction.bind"),
    INTER_UNBIND("interaction.unbind"),
    INTER_RELOAD_SINGLE("interaction.reload.single"),
    INTER_RELOAD_ALL("interaction.reload.all"),
    INTER_TRIGGER("interaction.trigger"),

    GUI_OPEN("gui.open"),

    STATS_GIVE("stats.modify.give"),
    STATS_TAKE("stats.modify.take"),
    STATS_SET("stats.modify.set"),
    STATS_RESET("stats.modify.reset"),
    STATS_VIEW("stats.view"),

    STATS_GLOBAL_GIVE("stats_global.modify.give"),
    STATS_GLOBAL_TAKE("stats_global.modify.take"),
    STATS_GLOBAL_SET("stats_global.modify.set"),
    STATS_GLOBAL_RESET("stats_global.modify.reset"),
    STATS_GLOBAL_VIEW("stats_global.view"),

    UNLOCKABLE_CREATE("unlockable.create"),
    UNLOCKABLE_DELETE("unlockable.delete"),
    UNLOCKABLE_GIVE("unlockable.modify.give"),
    UNLOCKABLE_TAKE("unlockable.modify.take"),

    UNLOCKABLE_TEMP_CREATE("unlockable_temp.create"),
    UNLOCKABLE_TEMP_DELETE("unlockable_temp.delete"),
    UNLOCKABLE_TEMP_GIVE("unlockable_temp.give"),
    UNLOCKABLE_TEMP_TAKE("unlockable_temp.take"),

    BALANCE_SELF("economy.balance.self"),
    BALANCE_OTHERS("economy.balance.others"),

    ECONOMY_GIVE("economy.modify.give"),
    ECONOMY_TAKE("economy.modify.take"),
    ECONOMY_SET("economy.modify.set"),
    ECONOMY_RESET("economy.modify.reset"),
    ECONOMY_RANDOM("economy.modify.random"),
    ECONOMY_CURRENCIES("economy.modify.currencies"),
    ECONOMY_COINFLIP("economy.coinflip"),

    ECONOMY_PAY("economy.pay"),

    NICK_REQUEST_SEND("nick.request.send"),
    NICK_REQUEST_VIEW("nick.request.view"),
    NICK_REQUEST_APPROVE("nick.request.approve"),
    NICK_REQUEST_DENY("nick.request.deny"),
    NICK_RESERVE("nick.reserve"),
    NICK_UNRESERVE("nick.unreserve"),
    NICK_RESET("nick.reset"),
    
    CHANNEL_CREATE("chat.channel.create"),
    CHANNEL_JOIN("chat.channel.join"),
    CHANNEL_LEAVE("chat.channel.leave"),
    CHANNEL_FOCUS("chat.channel.focus"),
    CHANNEL_UNFOCUS("chat.channel.unfocus"),
    CHANNEL_LIST("chat.channel.list"),
    CHANNEL_BROADCAST("chat.channel.broadcast"),
    CHANNEL_MODIFY("chat.channel.modify"),

    COSMETIC_TAKE("cosmetic.take"),
    COSMETIC_GIVE("cosmetic.give"),
    COSMETIC_SET("cosmetic.set"),

    LOOTTABLE_GIVE("loottable.give"),
    LOOTTABLE_RELOAD("loottable.reload"),

    COOLDOWNS_GIVE("cooldowns.give"),
    COOLDOWNS_REMOVE("cooldowns.remove"),
    COOLDOWNS_VIEW("cooldowns.view"),

    SYSTEMS("systems");




    private final String permission;

    Permissions(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }

}
