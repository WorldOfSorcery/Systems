package me.hektortm.woSSystems.economy.commands.subcommands;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.economy.commands.SubCommand;
import me.hektortm.woSSystems.economy.Currency;
import me.hektortm.woSSystems.economy.EcoManager;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.wosCore.Utils;
import me.hektortm.wosCore.logging.LogManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static me.hektortm.wosCore.Utils.error;

public class SetCommand extends SubCommand {
    private final EcoManager ecoManager;
    private final LogManager log;

    public SetCommand(EcoManager ecoManager, LogManager log) {
        this.ecoManager = ecoManager;
        this.log = log;
    }

    @Override
    public String getName() {
        return "set";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!PermissionUtil.hasAnyPermission(sender, Permissions.ECONOMY_SET)) return;

        if(args.length < 3) {
            error(sender, "economy", "error.set-usage");
            return;
        }

        String playerName = args[0];
        String currencyName = args[1].replace("_", " ");
        int amount;
        Currency currency = ecoManager.getCurrencies().get(currencyName.toLowerCase());
        String color = currency.getColor();

        try {
            amount = Integer.parseInt(args[2]);
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
            log.sendWarning(p.getName()+ "-> "+ target.getName() +": Set "+currencyName+" to "+amount);
            log.writeLog(p, "-> "+ target.getName() +": Set "+currencyName+" to "+amount);
        }

        ecoManager.modifyCurrency(target.getUniqueId(), currencyName, amount, EcoManager.Operation.SET);
        WoSSystems.ecoMsg3Values(sender, "economy", "currency.set", "%amount%", String.valueOf(amount), "%currency%", color+currencyName, "%player%", playerName);

    }
}
