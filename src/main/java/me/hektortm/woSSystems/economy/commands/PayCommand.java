package me.hektortm.woSSystems.economy.commands;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.economy.Currency;
import me.hektortm.woSSystems.economy.EcoManager;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.wosCore.LangManager;
import me.hektortm.wosCore.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static me.hektortm.wosCore.Utils.error;

public class PayCommand implements CommandExecutor {

    private final EcoManager ecoManager;
    private final LangManager lang;
    public PayCommand(EcoManager ecoManager, LangManager lang) {
        this.ecoManager = ecoManager;
        this.lang = lang;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
            if (!(sender instanceof Player)) {
                Utils.error(sender, "general", "error.notplayer");
                return true;
            }
            if(!PermissionUtil.hasPermission(sender, Permissions.ECONOMY_PAY)) return true;

            if(args.length > 3) {
                error(sender, "general", "error.pay-usage");
                return true;
            }

            String targetName = args[0];
            Player p = (Player) sender;
            String currencyName = args[1].replace("_", " ");

            int amount;
            Currency currency = ecoManager.getCurrencies().get(currencyName.toLowerCase());



            String color = currency.getColor();
            String icon = currency.getIcon();

            if (icon == null || icon.isBlank()) {
                icon = "";
            }



            try {
                amount = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                Utils.error(sender, "economy", "error.invalid-amount");

                return true;
            }

            if (amount <= 0) {
                Utils.error(sender, "economy", "error.invalid-amount-positive");
                return true;
            }

            Player target = Bukkit.getPlayer(targetName);
            if (target == null) {
                Utils.error(sender, "general", "error.online");
                return true;
            }

            if(ecoManager.hasEnoughCurrency(p.getUniqueId(), currencyName, amount)) {
                ecoManager.modifyCurrency(target.getUniqueId(), currencyName, amount, EcoManager.Operation.GIVE);
                ecoManager.modifyCurrency(p.getUniqueId(), currencyName, amount, EcoManager.Operation.TAKE);
                WoSSystems.ecoMsg3Values(target, "economy", "pay.target", "%amount%", color + amount, "%currency%", icon+ " " + currencyName, "%player%", p.getName());
                WoSSystems.ecoMsg3Values(sender, "economy", "pay.player", "%amount%", color + amount, "%currency%", icon+ " " + currencyName, "%target%", targetName);
                String actionbar = lang.getMessage("economy", "actionbar.given")
                        .replace("%icon%", icon)
                        .replace("%amount%", String.valueOf(amount))
                        .replace("%name%", currencyName)
                        .replace("%color%", color);
                target.sendActionBar(actionbar);
                String actionbar2 = lang.getMessage("economy", "actionbar.taken")
                        .replace("%icon%", icon)
                        .replace("%amount%", String.valueOf(amount))
                        .replace("%name%", currencyName)
                        .replace("%color%", color);
                p.sendActionBar(actionbar2);
            } else {
                error(p, "economy", "error.funds");
                return true;
            }
        return true;
    }
}
