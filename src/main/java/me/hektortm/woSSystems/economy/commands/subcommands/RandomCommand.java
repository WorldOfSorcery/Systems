package me.hektortm.woSSystems.economy.commands.subcommands;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.economy.commands.SubCommand;
import me.hektortm.woSSystems.economy.Currency;
import me.hektortm.woSSystems.economy.EcoManager;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.wosCore.LangManager;
import me.hektortm.wosCore.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Random;

import static me.hektortm.wosCore.Utils.error;

public class RandomCommand extends SubCommand {

    private final EcoManager ecoManager;
    private final LangManager lang;

    public RandomCommand(EcoManager ecoManager, LangManager lang) {
        this.ecoManager = ecoManager;
        this.lang = lang;
    }

    @Override
    public String getName() {
        return "random";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!PermissionUtil.hasPermission(sender, Permissions.ECONOMY_RANDOM)) return;

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
        ecoManager.modifyCurrency(target.getUniqueId(), currencyName, randomNumber, EcoManager.Operation.GIVE);
        WoSSystems.ecoMsg3Values(sender, "economy", "currency.given", "%amount%", String.valueOf(randomNumber), "%currency%", color+currencyName, "%player%", playerName);
        String actionbar = lang.getMessage("economy", "actionbar.given")
                .replace("%icon%", icon)
                .replace("%amount%", String.valueOf(randomNumber))
                .replace("%name%", currencyName)
                .replace("%color%", color);
        target.sendActionBar(actionbar);
    }
}
