package me.hektortm.woSSystems.systems.economy.cmd.sub;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.utils.model.Currency;
import me.hektortm.woSSystems.systems.economy.EcoManager;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
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
    public Permissions getPermission() {
        return Permissions.ECONOMY_CURRENCIES;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        if (ecoManager.getCurrencies().isEmpty()) {
            Utils.error(sender, "economy", "error.currencies");
            return;
        }

        WoSSystems.ecoMsg(sender,"economy","currency.list-header");
        for (Currency currency : ecoManager.getCurrencies().values()) {
            StringBuilder currencyDisplay = new StringBuilder(currency.getColor())
                    .append(currency.getName())
                    .append(" (")
                    .append(currency.getId()) // what the commands take
                    .append(")");

            if (currency.getIcon() != null && !currency.getIcon().isEmpty()) {
                currencyDisplay.append(" ").append(currency.getIcon());
            }

            sender.sendMessage(Utils.parseColorCodes(currencyDisplay.toString())); // § codes and #RRGGBB

        }
    }
}
