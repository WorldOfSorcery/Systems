package me.hektortm.woSSystems.systems.economy;

import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.utils.Operations;
import me.hektortm.woSSystems.utils.model.Currency;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Service layer for the economy system.
 *
 * <p>Provides high-level currency operations (give, take, set, reset) by
 * computing the new balance and delegating persistence to
 * {@link me.hektortm.woSSystems.database.dao.EconomyDAO} via the
 * {@link DAOHub}.  Only online players can have their currency modified;
 * calls for offline players are silently ignored.</p>
 */
public class EcoManager {
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final DAOHub hub;

    /**
     * @param hub the DAO hub used to access economy persistence
     */
    public EcoManager(DAOHub hub) {
        this.hub = hub;
    }

    /**
     * Modifies a currency balance for an online player.  The operation is
     * computed locally and then persisted via the DAO.  {@link Operations#TAKE}
     * will floor the balance at zero rather than allowing negatives.
     *
     * @param uuid      the player's UUID (must be online)
     * @param currency  the currency ID to modify
     * @param amount    the amount to use for the operation
     * @param operation the arithmetic operation to apply
     */
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
        hub.getEconomyDAO().updatePlayerCurrency(player.getUniqueId(), currency, newAmount);
    }

    /**
     * Records an economy audit log entry for the given currency change.
     * The previous balance is fetched automatically; the new balance is
     * {@code previousAmount + changeAmount}.
     *
     * @param uuid         the player's UUID
     * @param currency     the affected currency ID
     * @param changeAmount the delta applied (positive for gains, negative for losses)
     * @param sourceType   category of the source (e.g. {@code "interaction"})
     * @param source       specific source identifier (e.g. interaction ID)
     */
    public void ecoLog(UUID uuid, String currency, long changeAmount, String sourceType, String source) {
        long previousAmount = getCurrencyBalance(uuid, currency);
        hub.getEconomyDAO().ecoLog(uuid, currency, previousAmount, previousAmount+changeAmount, changeAmount, sourceType, source);
    }

    /**
     * Returns all currency definitions keyed by currency ID.
     *
     * @return map of currency ID to {@link Currency}
     */
    public Map<String, Currency> getCurrencies() {
        return hub.getEconomyDAO().getCurrencies(); // Fetch currencies from the database
    }

    /**
     * Returns the current balance for a player and currency.
     * Served from cache for online players; falls back to a DB query for offline players.
     *
     * @param uuid     the player's UUID
     * @param currency the currency ID
     * @return the current balance, or {@code 0} if the player has no entry
     */
    public long getCurrencyBalance(UUID uuid, String currency) {
        return hub.getEconomyDAO().getPlayerCurrency(uuid, currency);

    }

    /**
     * Returns {@code true} if a currency with the given ID is defined.
     *
     * @param id the currency ID to check
     * @return {@code true} if the currency exists
     */
    public boolean currencyExists(String id) {
        return hub.getEconomyDAO().currencyExists(id);
    }

    /**
     * Returns {@code true} if the player's balance in the given currency is at
     * least {@code amount}.
     *
     * @param uuid     the player's UUID
     * @param currency the currency ID
     * @param amount   the required minimum balance
     * @return {@code true} if the player can afford the cost
     */
    public boolean hasEnoughCurrency(UUID uuid, String currency, long amount) {
        return getCurrencyBalance(uuid, currency) >= amount;
    }

}
