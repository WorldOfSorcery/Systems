package me.hektortm.woSSystems.economy.commands.subcommands;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.economy.commands.SubCommand;
import me.hektortm.woSSystems.economy.Currency;
import me.hektortm.woSSystems.economy.EcoManager;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.wosCore.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static me.hektortm.wosCore.Utils.error;

public class ResetCommand extends SubCommand {

    private final EcoManager ecoManager;
    public ResetCommand(EcoManager ecoManager) {
        this.ecoManager = ecoManager;
    }

    @Override
    public String getName() {
        return "reset";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!PermissionUtil.hasAnyPermission(sender, Permissions.ECONOMY_RESET)) return;

        if(args.length < 3) {
            error(sender, "economy", "error.reset-usage");
            return;
        }

        String playerName = args[0];
        String currencyName = args[1].replace("_", " ");
        Currency currency = ecoManager.getCurrencies().get(currencyName.toLowerCase());
        String color = currency.getColor();

        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            Utils.error(sender, "general", "error.online");
            return;
        }
        ecoManager.modifyCurrency(target.getUniqueId(), currencyName, 0, EcoManager.Operation.RESET);
        WoSSystems.ecoMsg2Values(sender, "economy", "currency.reset",  "%currency%", color+currencyName, "%player%", playerName);

    }
}
