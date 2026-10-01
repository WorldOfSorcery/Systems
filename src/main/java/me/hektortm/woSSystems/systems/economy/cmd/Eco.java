package me.hektortm.woSSystems.systems.economy.cmd;

import me.hektortm.woSSystems.systems.economy.Amounts;
import me.hektortm.woSSystems.systems.economy.EcoManager;
import me.hektortm.woSSystems.utils.model.Currency;
import me.hektortm.wosCore.LangManager;
import me.hektortm.wosCore.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.OptionalLong;

/**
 * What every economy command does the same way: find the currency and the
 * player that were typed, read the amount, and show the result. Each lookup
 * tells the sender what was wrong and returns null / empty, so a command reads
 * as "get the three things, then act".
 */
public final class Eco {

    private Eco() {}

    /** The currency typed (id or name), or null after telling the sender it doesn't exist. */
    public static Currency currency(EcoManager eco, CommandSender sender, String typed) {
        Currency currency = eco.findCurrency(typed);
        if (currency == null) Utils.error(sender, "economy", "error.currency-exist");
        return currency;
    }

    /**
     * The amount typed ("1,000", "10k", "all" …), or empty after telling the
     * sender it isn't one. {@code all}: what "all" stands for, negative if it
     * has no meaning here.
     */
    public static OptionalLong amount(CommandSender sender, String typed, long all) {
        OptionalLong amount = Amounts.parse(typed, all);
        if (amount.isEmpty()) Utils.error(sender, "economy", "error.invalid-amount");
        return amount;
    }

    /** As {@link #amount}, and it must be above zero. */
    public static OptionalLong positiveAmount(CommandSender sender, String typed, long all) {
        OptionalLong amount = amount(sender, typed, all);
        if (amount.isPresent() && amount.getAsLong() <= 0) {
            Utils.error(sender, "economy", "error.invalid-amount-positive");
            return OptionalLong.empty();
        }
        return amount;
    }

    /** The online player typed, or null after telling the sender they aren't online. */
    public static Player online(CommandSender sender, String name) {
        Player player = Bukkit.getPlayerExact(name);
        if (player == null) Utils.error(sender, "general", "error.online");
        return player;
    }

    /** The currency's name in its colour. */
    public static String label(Currency currency) {
        return currency.getColor() + currency.getName();
    }

    /** The currency's icon, "" when it has none. */
    public static String icon(Currency currency) {
        String icon = currency.getIcon();
        return icon == null || icon.isBlank() ? "" : icon;
    }

    /** "+ 1,000 Gold" over the hotbar: the message {@code key} with the currency's icon, colour and name. */
    @SuppressWarnings("deprecation") // the lang files are legacy-formatted strings
    public static void actionBar(Player player, LangManager lang, String key, Currency currency, long amount) {
        String text = lang.getMessage("economy", key)
                .replace("%icon%", icon(currency))
                .replace("%amount%", Amounts.format(amount))
                .replace("%name%", currency.getName())
                .replace("%color%", currency.getColor());
        player.sendActionBar(Utils.replaceColorPlaceholders(text));
    }
}
