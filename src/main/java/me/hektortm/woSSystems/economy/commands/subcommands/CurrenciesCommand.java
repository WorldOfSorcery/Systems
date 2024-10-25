package me.hektortm.woSSystems.economy.commands.subcommands;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.economy.commands.SubCommand;
import me.hektortm.woSSystems.economy.Currency;
import me.hektortm.woSSystems.economy.EcoManager;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import org.bukkit.command.CommandSender;

public class CurrenciesCommand extends SubCommand {

    private final EcoManager ecoManager;
    public CurrenciesCommand(EcoManager ecoManager) {
        this.ecoManager = ecoManager;
    }

    @Override
    public String getName() {
        return "currencies";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!PermissionUtil.hasPermission(sender, Permissions.ECONOMY_CURRENCIES)) return;

        if (ecoManager.getCurrencies().isEmpty()) {
            WoSSystems.ecoMsg(sender, "economy", "currency.notfound");
            return;
        }

        WoSSystems.ecoMsg(sender,"economy","currency.list-header");
        for (Currency currency : ecoManager.getCurrencies().values()) {
            StringBuilder currencyDisplay = new StringBuilder(currency.getColor())
                    .append(currency.getName())
                    .append(" (")
                    .append(currency.getShortName())
                    .append(")");

            if (currency.getIcon() != null && !currency.getIcon().isEmpty()) {
                currencyDisplay.append(" ").append(currency.getIcon());
            }

            sender.sendMessage(currencyDisplay.toString());

        }
    }
}
