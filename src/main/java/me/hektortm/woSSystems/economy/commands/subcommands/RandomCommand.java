package me.hektortm.woSSystems.economy.commands.subcommands;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.utils.dataclasses.Currency;
import me.hektortm.woSSystems.economy.EcoManager;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.LangManager;
import me.hektortm.wosCore.Utils;
import me.hektortm.wosCore.logging.LogManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Random;

import static me.hektortm.wosCore.Utils.error;

@SuppressWarnings({"DuplicatedCode", "deprecation"})
public class RandomCommand extends SubCommand {

    private final EcoManager ecoManager;
    private final LangManager lang;
    private final LogManager log;

    public RandomCommand(EcoManager ecoManager, LangManager lang, LogManager log) {
        this.ecoManager = ecoManager;
        this.lang = lang;
        this.log = log;
    }

    @Override
    public String getName() {
        return "random";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.ECONOMY_RANDOM;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        if(args.length < 4) {
            error(sender, "economy", "error.random-usage");
            return;
        }

        String playerName = args[0];
        String currencyName = args[1].replace("_", " ");
        int amount1;
        int amount2;
        Random random = new Random();
        int randomNumber;
        Currency currency = ecoManager.getCurrencies().get(currencyName.toLowerCase());
        String color = currency.getColor();
        String icon = currency.getIcon();

        if (icon == null || icon.isBlank()) {
            icon = "";
        }

        try {
            amount1 = Integer.parseInt(args[2]);
            amount2 = Integer.parseInt(args[3]);
            randomNumber = random.nextInt((amount2 - amount1)+1) +amount1;
        } catch (NumberFormatException e) {
            WoSSystems.ecoMsg(sender, "economy", "invalid-amount");
            return;
        }

        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            Utils.error(sender, "general", "error.online");
            return;
        }

        if (sender instanceof Player p && target.getName().equals(p.getName())) {
            log.sendWarning(p.getName()+ "-> "+ target.getName() +": Random give "+randomNumber+" "+currencyName);
            log.writeLog(p, "-> "+ target.getName() +": Random give "+randomNumber+" "+currencyName);
        }

        ecoManager.modifyCurrency(target.getUniqueId(), currencyName, randomNumber, EcoManager.Operation.GIVE);
        WoSSystems.ecoMsg3Values(sender, "economy", "currency.given", "%amount%", String.valueOf(randomNumber), "%currency%", color+currencyName, "%player%", playerName);
        String actionbar = lang.getMessage("economy", "actionbar.given")
                .replace("%icon%", icon)
                .replace("%amount%", String.valueOf(randomNumber))
                .replace("%name%", currencyName)
                .replace("%color%", color);
        target.sendActionBar(actionbar); /* Deprecated */
    }
}
