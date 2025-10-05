package me.hektortm.woSSystems.utils;

import java.util.ArrayList;
import java.util.List;

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
    CITEM_EQUIPPABLE("citem.modify.equippable"),

    CRECIPE_CREATE("crecipe.create"),
    CRECIPE_DELETE("crecipe.delete"),
    CRECIPE_RELOAD("crecipe.reload"),

    INTER_BIND("interaction.bind"),
    INTER_UNBIND("interaction.unbind"),
    INTER_RELOAD_SINGLE("interaction.reload.single"),
    INTER_RELOAD_ALL("interaction.reload.all"),
    INTER_TRIGGER("interaction.trigger"),
    INTER_INFO("interaction.info"),

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
    CHANNEL_DELETE("chat.channel.delete"),

    COSMETIC_TAKE("cosmetic.take"),
    COSMETIC_GIVE("cosmetic.give"),
    COSMETIC_SET("cosmetic.set"),
    COSMETC_MODIFY("cosmetic.modify"),
    COSMETIC_DELETE("cosmetic.delete"),

    DIALOG_TRIGGER("dialog.trigger"),

    LOOTTABLE_GIVE("loottable.give"),
    LOOTTABLE_RELOAD("loottable.reload"),

    COOLDOWNS_GIVE("cooldowns.give"),
    COOLDOWNS_REMOVE("cooldowns.remove"),
    COOLDOWNS_VIEW("cooldowns.view"),

    PORTAL_CHANNELS_VIEW("portal.channels.view"),
    PORTAL_CHANNELS_CREATE("portal.channels.create"),
    PORTAL_CHANNELS_DELETE("portal.channels.delete"),
    PORTAL_CHANNELS_MODIFY("portal.channels.modify"),

    PORTAL_CITEMS_VIEW("portal.citems.view"),

    PORTAL_COOLDOWNS_CREATE("portal.cooldowns.create"),
    PORTAL_COOLDOWNS_DELETE("portal.cooldowns.delete"),
    PORTAL_COOLDOWNS_MODIFY("portal.cooldowns.modify"),
    PORTAL_COOLDOWNS_VIEW("portal.cooldowns.view"),

    PORTAL_CONSTANTS_CREATE("portal.constants.create"),
    PORTAL_CONSTANTS_DELETE("portal.constants.delete"),
    PORTAL_CONSTANTS_MODIFY("portal.constants.modify"),
    PORTAL_CONSTANTS_VIEW("portal.constants.view"),

    PORTAL_COSMETICS_VIEW("portal.cosmetics.view"),
    PORTAL_COSMETICS_CREATE("portal.cosmetics.create"),
    PORTAL_COSMETICS_DELETE("portal.cosmetics.delete"),
    PORTAL_COSMETICS_MODIFY("portal.cosmetics.modify"),

    PORTAL_CURRENCIES_VIEW("portal.currencies.view"),
    PORTAL_CURRENCIES_CREATE("portal.currencies.create"),
    PORTAL_CURRENCIES_DELETE("portal.currencies.delete"),
    PORTAL_CURRENCIES_MODIFY("portal.currencies.modify"),

    PORTAL_DIALOGS_VIEW("portal.dialogs.view"),
    PORTAL_DIALOGS_CREATE("portal.dialogs.create"),
    PORTAL_DIALOGS_DELETE("portal.dialogs.delete"),
    PORTAL_DIALOGS_MODIFY("portal.dialogs.modify"),

    PORTAL_FISHING_VIEW("portal.fishing.view"),
    PORTAL_FISHING_CREATE("portal.fishing.create"),
    PORTAL_FISHING_DELETE("portal.fishing.delete"),
    PORTAL_FISHING_MODIFY("portal.fishing.modify"),

    PORTAL_GUIS_VIEW("portal.guis.view"),
    PORTAL_GUIS_CREATE("portal.guis.create"),
    PORTAL_GUIS_DELETE("portal.guis.delete"),
    PORTAL_GUIS_MODIFY("portal.guis.modify"),

    PORTAL_INTERACTIONS_VIEW("portal.interactions.view"),
    PORTAL_INTERACTIONS_CREATE("portal.interactions.create"),
    PORTAL_INTERACTIONS_DELETE("portal.interactions.delete"),
    PORTAL_INTERACTIONS_MODIFY("portal.interactions.modify"),

    PORTAL_LOOTTABLES_VIEW("portal.loottables.view"),
    PORTAL_LOOTTABLES_CREATE("portal.loottables.create"),
    PORTAL_LOOTTABLES_DELETE("portal.loottables.delete"),
    PORTAL_LOOTTABLES_MODIFY("portal.loottables.modify"),

    PORTAL_RECIPES_VIEW("portal.recipes.view"),
    PORTAL_RECIPES_CREATE("portal.recipes.create"),
    PORTAL_RECIPES_DELETE("portal.recipes.delete"),
    PORTAL_RECIPES_MODIFY("portal.recipes.modify"),

    PORTAL_STATS_VIEW("portal.stats.view"),
    PORTAL_STATS_CREATE("portal.stats.create"),
    PORTAL_STATS_DELETE("portal.stats.delete"),
    PORTAL_STATS_MODIFY("portal.stats.modify"),

    PORTAL_TIMEEVENTS_VIEW("portal.timeevents.view"),
    PORTAL_TIMEEVENTS_CREATE("portal.timeevents.create"),
    PORTAL_TIMEEVENTS_DELETE("portal.timeevents.delete"),
    PORTAL_TIMEEVENTS_MODIFY("portal.timeevents.modify"),

    PORTAL_UNLOCKABLES_VIEW("portal.unlockables.view"),
    PORTAL_UNLOCKABLES_CREATE("portal.unlockables.create"),
    PORTAL_UNLOCKABLES_DELETE("portal.unlockables.delete"),
    PORTAL_UNLOCKABLES_MODIFY("portal.unlockables.modify"),

    PORTAL_PERMISSIONS_GROUPS_VIEW("portal.permissions.groups.view"),
    PORTAL_PERMISSIONS_GROUPS_CREATE("portal.permissions.groups.create"),
    PORTAL_PERMISSIONS_GROUPS_DELETE("portal.permissions.groups.delete"),
    PORTAL_PERMISSIONS_GROUPS_MODIFY("portal.permissions.groups.modify"),

    PORTAL_UNLOCK("portal.unlock"),
    PORTAL_REQUEST_APPROVE("portal.request.approve"),
    PORTAL_REQUEST_DENY("portal.request.deny"),

    PORTAL_PERMISSIONS_USERS_VIEW("portal.permissions.users.view"),
    PORTAL_PERMISSIONS_USERS_MODIFY("portal.permissions.users.modify");

    private final String permission;

    Permissions(String permission) {
        this.permission = permission;
    }


    public String getPermission() {
        return permission;
    }

}
