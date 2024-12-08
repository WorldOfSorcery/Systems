package me.hektortm.woSSystems.utils;

import me.hektortm.woSSystems.economy.EcoManager;
import me.hektortm.woSSystems.systems.citems.CitemManager;
import me.hektortm.woSSystems.systems.stats.StatsManager;
import me.hektortm.woSSystems.systems.unlockables.UnlockableManager;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

public class ConditionHandler {

    private final UnlockableManager unlockableManager;
    private final StatsManager statsManager;
    private final EcoManager ecoManager;
    private final CitemManager citemManager;

    public ConditionHandler(UnlockableManager unlockableManager, StatsManager statsManager, EcoManager ecoManager, CitemManager citemManager) {
        this.unlockableManager = unlockableManager;
        this.statsManager = statsManager;
        this.ecoManager = ecoManager;
        this.citemManager = citemManager;
    }

    public boolean validateConditions(Player player, JSONObject conditions) {
        if (conditions == null) {
            return true; // No conditions mean the recipe is allowed
        }

        for (Object conditionType : conditions.keySet()) {
            String type = conditionType.toString();
            JSONObject specificConditions = (JSONObject) conditions.get(conditionType);

            switch (type) {
                case "unlockable":
                    if (!validateUnlockable(player, specificConditions)) {
                        return false;
                    }
                    break;

                case "stats":
                    if (!validateStats(player, specificConditions)) {
                        return false;
                    }
                    break;
                case "permission":
                    if(!validatePermission(player, specificConditions)) {
                        return false;
                    }
                    break;
                case "citem":
                    if(!validateCitem(player, specificConditions)) {
                        return false;
                    }
                    break;
                case "currency":
                    if (!validateCurrency(player, specificConditions)) {
                        return false;
                    }
                    break;

                default:
                    throw new IllegalArgumentException("Unknown condition type: " + type);
            }
        }

        return true;
    }



    private boolean validateUnlockable(Player player, JSONObject unlockables) {
        for (Object key : unlockables.keySet()) {
            String unlockableId = key.toString();
            boolean requiredState = (boolean) unlockables.get(key);

            // Replace this with your actual logic to check if a player has the unlockable
            boolean playerHasUnlockable = unlockableManager.getPlayerUnlockable(player, unlockableId);

            if (playerHasUnlockable != requiredState) {
                return false;
            }
        }

        return true;
    }

    private boolean validateCurrency(Player player, JSONObject currency) {
        for (Object key : currency.keySet()) {
            String currencyId = key.toString();
            long requiredAmount = (long) currency.get(currencyId);
            long playerAmount = ecoManager.getCurrencyBalance(player.getUniqueId(), currencyId);

            if (playerAmount < requiredAmount) {
                return false;
            }
        }
        return true;
    }

    private boolean validatePermission(Player player, JSONObject permission) {
        for (Object key : permission.keySet()) {
            String permissionId = key.toString();
            boolean requiredState = (boolean) permission.get(key);

            boolean playerHasPermission = hasPerms(player, permissionId);
            if (playerHasPermission != requiredState) {
                return false;
            }
        }
        return true;
    }

    private boolean hasPerms(Player player, String permission) {
        return player.hasPermission(permission);
    }

    private boolean validateCitem(Player player, JSONObject citem) {
        for (Object key : citem.keySet()) {
            String citemId = key.toString();
            int requiredAmount = ((Number) citem.get(citemId)).intValue();

            int playerAmount = citemManager.citemAmount(player, citemId);

            if (playerAmount < requiredAmount) {
                return false;
            }
        }

        return true; // All conditions passed
    }


    private boolean validateStats(Player player, JSONObject stats) {
        for (Object key : stats.keySet()) {
            String stat = key.toString();
            long requiredValue = ((Number) stats.get(key)).longValue();

            // Replace this with your actual logic to check the player's stats
            long playerStatValue = statsManager.getPlayerStat(player.getUniqueId(), stat);

            if (playerStatValue < requiredValue) {
                return false;
            }
        }

        return true;
    }

}
