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

import static me.hektortm.wosCore.Utils.error;

public class GiveCommand extends SubCommand {

    private final EcoManager ecoManager;
    private final LangManager lang;
    private final LogManager log;

    public GiveCommand(EcoManager ecoManager, LangManager lang, LogManager log) {
        this.ecoManager = ecoManager;
        this.lang = lang;
        this.log = log;
    }

    @Override
    public String getName() {
        return "give";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.ECONOMY_GIVE;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        if(args.length < 3) {
            error(sender, "economy", "error.give-usage");
            return;
        }

        String playerName = args[0];
        String currencyID = args[1];
        int amount;
        Currency currency = ecoManager.getCurrencies().get(currencyID.toLowerCase());
        String name = currency.getName();
        String color = currency.getColor();
        String icon = currency.getIcon();

        if (icon == null || icon.isBlank()) {
            icon = "";
        }


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
                log.sendWarning(p.getName()+ "-> "+ target.getName() +": Gave "+amount+" "+currencyID);
                log.writeLog(p, "-> "+ target.getName() +": Gave "+amount+" "+currencyID);
        }

        ecoManager.modifyCurrency(target.getUniqueId(), currencyID, amount, EcoManager.Operation.GIVE);
        WoSSystems.ecoMsg3Values(sender, "economy", "currency.given", "%amount%", String.valueOf(amount), "%currency%", color+name, "%player%", playerName);

        String actionbar = lang.getMessage("economy", "actionbar.given")
                .replace("%icon%", icon)
                .replace("%amount%", String.valueOf(amount))
                .replace("%name%", name)
                .replace("%color%", color);
        target.sendActionBar(actionbar);

    }
}
