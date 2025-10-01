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

@SuppressWarnings({"DuplicatedCode"})
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
    public Permissions getPermission() {
        return Permissions.ECONOMY_SET;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        if(args.length < 3) {
            error(sender, "economy", "error.set-usage");
            return;
        }

        String playerName = args[0];
        String currencyID = args[1];
        if(!ecoManager.currencyExists(currencyID)) {
            error(sender, "economy", "currency-exist");
            return;
        }
        int amount;
        Currency currency = ecoManager.getCurrencies().get(currencyID.toLowerCase());
        String name = currency.getName();
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
            log.sendWarning(p.getName()+ "-> "+ target.getName() +": Set "+name+" to "+amount);
            log.writeLog(p, "-> "+ target.getName() +": Set "+name+" to "+amount);
        }

        ecoManager.modifyCurrency(target.getUniqueId(), currencyID, amount, Operations.SET);
        WoSSystems.ecoMsg3Values(sender, "economy", "currency.set", "%amount%", String.valueOf(amount), "%currency%", color+name, "%player%", playerName);

    }
}
