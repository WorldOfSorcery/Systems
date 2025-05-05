package me.hektortm.woSSystems.economy.commands.subcommands;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.utils.Operations;
import me.hektortm.woSSystems.utils.dataclasses.Currency;
import me.hektortm.woSSystems.economy.EcoManager;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import me.hektortm.wosCore.logging.LogManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static me.hektortm.wosCore.Utils.error;

public class ResetCommand extends SubCommand {

    private final EcoManager ecoManager;
    private final LogManager log;

    public ResetCommand(EcoManager ecoManager, LogManager log) {
        this.ecoManager = ecoManager;
        this.log = log;
    }

    @Override
    public String getName() {
        return "reset";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.ECONOMY_RESET;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        if(args.length < 3) {
            error(sender, "economy", "error.reset-usage");
            return;
        }

        String playerName = args[0];
        String currencyName = args[1];
        Currency currency = ecoManager.getCurrencies().get(currencyName.toLowerCase());
        String name = currency.getName();
        String color = currency.getColor();

        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            Utils.error(sender, "general", "error.online");
            return;
        }

        if (sender instanceof Player p && target.getName().equals(p.getName())) {
            log.sendWarning(p.getName()+ "-> "+ target.getName() +": Reset "+name);
            log.writeLog(p, "-> "+ target.getName() +": Reset "+name);
        }

        ecoManager.modifyCurrency(target.getUniqueId(), currencyName, 0, Operations.RESET);
        WoSSystems.ecoMsg2Values(sender, "economy", "currency.reset",  "%currency%", color+name, "%player%", playerName);

    }
}
