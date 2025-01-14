package me.hektortm.woSSystems.utils;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.economy.EcoManager;
import me.hektortm.woSSystems.systems.citems.CitemManager;
import me.hektortm.woSSystems.systems.stats.StatsManager;
import me.hektortm.woSSystems.systems.unlockables.UnlockableManager;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConditionHandler_new {

    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final UnlockableManager unlockableManager;
    private final StatsManager statsManager;
    private final EcoManager ecoManager;
    private final CitemManager citemManager;
    private PlaceholderResolver resolver;

    public ConditionHandler_new(UnlockableManager unlockableManager, StatsManager statsManager, EcoManager ecoManager, CitemManager citemManager) {
        this.unlockableManager = unlockableManager;
        this.statsManager = statsManager;
        this.ecoManager = ecoManager;
        this.citemManager = citemManager;
        resolver  = new PlaceholderResolver(statsManager, citemManager);
    }

    public UnmetConditionOutcomes getUnmetConditionOutcomes(Player player, JSONObject conditions) {
        UnmetConditionOutcomes unmetOutcomes = new UnmetConditionOutcomes();

        if (conditions == null) {
            return unmetOutcomes; // No conditions mean nothing to check
        }

        for (Object key : conditions.keySet()) {
            String conditionKey = key.toString();
            JSONObject conditionData = (JSONObject) conditions.get(conditionKey);

            // Split the condition key into type and specific identifier
            String[] parts = conditionKey.split(":");
            String conditionType = parts[0];
            String conditionIdentifier = parts.length > 1 ? parts[1] : null;

            // Evaluate the condition based on its type
            boolean isMet = evaluateCondition(player, conditionType, conditionIdentifier, conditionData);

            // If the condition is not met, collect the "false" branch outcomes
            if (!isMet) {
                JSONObject falseOutcome = (JSONObject) conditionData.get("false");

                if (falseOutcome != null) {
                    // Collect actions
                    if (falseOutcome.containsKey("actions")) {
                        JSONArray actions = (JSONArray) falseOutcome.get("actions");
                        for (Object actionObj : actions) {
                            unmetOutcomes.actions.add(actionObj.toString());
                        }
                    }

                    // Collect holograms
                    if (falseOutcome.containsKey("hologram")) {
                        JSONArray hologram = (JSONArray) falseOutcome.get("hologram");
                        for (Object lineObj : hologram) {
                            unmetOutcomes.holograms.add(lineObj.toString());
                        }
                    }

                    // Collect particles
                    if (falseOutcome.containsKey("particles")) {
                        JSONObject particles = (JSONObject) falseOutcome.get("particles");
                        String type = (String) particles.get("type");
                        String color = (String) particles.get("color");
                        unmetOutcomes.particleData.put(type, color);
                    }
                }
            }
        }

        return unmetOutcomes;
    }

    private boolean evaluateCondition(Player player, String conditionType, String conditionIdentifier, JSONObject conditionData) {
        switch (conditionType) {
            case "has_citem":
                return citemManager.citemAmount(player, conditionIdentifier) > 0;

            case "stats":
                long playerStatValue = statsManager.getPlayerStat(player.getUniqueId(), conditionIdentifier);
                long requiredValue = ((Number) conditionData.get("value")).longValue();
                return playerStatValue >= requiredValue;

            case "currency":
                long playerBalance = ecoManager.getCurrencyBalance(player.getUniqueId(), conditionIdentifier);
                long requiredBalance = ((Number) conditionData.get("value")).longValue();
                return playerBalance >= requiredBalance;

            case "permission":
                return player.hasPermission(conditionIdentifier);

            case "unlockable":
                return unlockableManager.getPlayerUnlockable(player, conditionIdentifier);

            default:
                plugin.getLogger().warning("Unknown condition type: " + conditionType);
                return false;
        }
    }

    // Existing methods like executeOutcome, executeActions, etc., remain unchanged.

    /**
     * Helper class to collect unmet condition outcomes.
     */
    public static class UnmetConditionOutcomes {
        public final List<String> actions = new ArrayList<>();
        public final List<String> holograms = new ArrayList<>();
        public final Map<String, String> particleData = new HashMap<>();
    }



    private boolean validateUnlockable(Player player, JSONObject unlockables) {
        for (Object key : unlockables.keySet()) {
            String unlockableId = key.toString();

            // Skip "else" block in conditions
            if (key.equals("else")) {
                continue;  // Skip processing for "else"
            }

            Object conditionValue = unlockables.get(key);

            // Check if the value is a Boolean (the actual condition)
            if (conditionValue instanceof Boolean) {
                boolean requiredState = (Boolean) conditionValue;
                boolean playerHasUnlockable = unlockableManager.getPlayerUnlockable(player, unlockableId);

                if (playerHasUnlockable != requiredState) {
                    return false;
                }
            } else {
                // Handle unexpected types or log an error for debugging
                plugin.getLogger().warning("Expected Boolean for unlockable condition but found: " + conditionValue.getClass().getSimpleName());
            }
        }

        return true;
    }


    private boolean validateCurrency(Player player, JSONObject currency) {
        for (Object key : currency.keySet()) {
            String currencyId = key.toString();
            if (currency.get(key) instanceof JSONArray && currency.get(key).equals("else")) {
                continue;
            }
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
            if (permission.get(key) instanceof JSONArray && permission.get(key).equals("else")) {
                continue;
            }
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

    private boolean validateCitem(Player player, JSONObject conditions) {
        for (Object key : conditions.keySet()) {
            String conditionKey = key.toString();

            // Skip the "else" block if present
            if (conditionKey.equals("else")) {
                continue;
            }

            Object conditionValue = conditions.get(key);

            // Check if the value is a Number (e.g., for validating item conditions)
            if (conditionValue instanceof Number) {
                Number requiredAmount = (Number) conditionValue;

                // Compare the player's item count with the required amount
                int playerItemCount = citemManager.citemAmount(player, conditionKey);  // Modify this to get the player's item count
                if (playerItemCount < requiredAmount.intValue()) {
                    return false;
                }
            } else if (conditionValue instanceof JSONArray) {
                // Handle case where conditionValue is an array (e.g., multiple possible valid values)
                JSONArray valuesArray = (JSONArray) conditionValue;
                boolean valid = false;

                // You could check if the player's item count or condition matches any of the values in the array
                for (Object arrayValue : valuesArray) {
                    if (arrayValue instanceof Number) {
                        if (citemManager.citemAmount(player, conditionKey) >= ((Number) arrayValue).intValue()) {
                            valid = true;
                            break;
                        }
                    }
                }

                if (!valid) {
                    return false;
                }
            } else {
                // Log an error if the condition value is neither a Number nor a JSONArray
                plugin.getLogger().warning("Unexpected condition value type: " + conditionValue.getClass().getSimpleName());
            }
        }

        return true;
    }



    private boolean validateStats(Player player, JSONObject stats) {
        for (Object key : stats.keySet()) {
            String stat = key.toString();
            if (stats.get(key) instanceof JSONArray && stats.get(key).equals("else")) {
                continue;
            }
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
