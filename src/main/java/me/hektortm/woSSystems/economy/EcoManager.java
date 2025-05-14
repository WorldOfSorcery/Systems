package me.hektortm.woSSystems.economy;

import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.utils.Operations;
import me.hektortm.woSSystems.utils.dataclasses.Currency;
import me.hektortm.wosCore.WoSCore;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public class EcoManager {
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final DAOHub hub;

    public EcoManager(DAOHub hub) {
        this.hub = hub;
    }

    public void modifyCurrency(UUID uuid, String currency, long amount, Operations operation) {
        Player player = plugin.getServer().getPlayer(uuid);
        if (player == null) return; // Ensure player is online


        long currentAmount = getCurrencyBalance(uuid, currency);
        long newAmount = currentAmount;

        switch (operation) {
            case GIVE:
                newAmount += amount;
                break;
            case TAKE:
                newAmount = Math.max(0, currentAmount - amount);
                break;
            case SET:
                newAmount = amount;
                break;
            case RESET:
                newAmount = 0;
                break;
        }
        hub.getEconomyDAO().updatePlayerCurrency(player, currency, newAmount);

    }

    public Map<String, Currency> getCurrencies() {
        return hub.getEconomyDAO().getCurrencies(); // Fetch currencies from the database
    }

    public long getCurrencyBalance(UUID uuid, String currency) {
        Player player = plugin.getServer().getPlayer(uuid);
        if (player == null) return 0; // Default to 0 if player isn't found


        return hub.getEconomyDAO().getPlayerCurrency(player, currency);

    }

    public boolean hasEnoughCurrency(UUID uuid, String currency, long amount) {
        return getCurrencyBalance(uuid, currency) >= amount;
    }

}
