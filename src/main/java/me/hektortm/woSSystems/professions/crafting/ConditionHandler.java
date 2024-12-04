package me.hektortm.woSSystems.professions.crafting;

import me.hektortm.woSSystems.systems.stats.StatsManager;
import me.hektortm.woSSystems.systems.unlockables.UnlockableManager;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

public class ConditionHandler {

    private final UnlockableManager unlockableManager;
    private final StatsManager statsManager;

    public ConditionHandler(UnlockableManager unlockableManager, StatsManager statsManager) {
        this.unlockableManager = unlockableManager;
        this.statsManager = statsManager;
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
