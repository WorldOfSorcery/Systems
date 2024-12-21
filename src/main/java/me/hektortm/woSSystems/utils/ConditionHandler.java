package me.hektortm.woSSystems.utils;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.economy.EcoManager;
import me.hektortm.woSSystems.systems.citems.CitemManager;
import me.hektortm.woSSystems.systems.stats.StatsManager;
import me.hektortm.woSSystems.systems.unlockables.UnlockableManager;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class ConditionHandler {

    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final UnlockableManager unlockableManager;
    private final StatsManager statsManager;
    private final EcoManager ecoManager;
    private final CitemManager citemManager;
    private PlaceholderResolver resolver;

    public ConditionHandler(UnlockableManager unlockableManager, StatsManager statsManager, EcoManager ecoManager, CitemManager citemManager) {
        this.unlockableManager = unlockableManager;
        this.statsManager = statsManager;
        this.ecoManager = ecoManager;
        this.citemManager = citemManager;
        resolver  = new PlaceholderResolver(statsManager, citemManager);
    }

    public boolean validateConditions(Player player, JSONObject conditions) {
        if (conditions == null) {
            return true; // No conditions mean the recipe is allowed
        }

        for (Object conditionType : conditions.keySet()) {
            String type = conditionType.toString();
            JSONObject specificConditions = (JSONObject) conditions.get(conditionType);
            JSONArray elseActions = null;
            if (specificConditions.get("else") != null) {
                elseActions = (JSONArray) specificConditions.get("else");
            }

            switch (type) {
                case "unlockable":
                    if (!validateUnlockable(player, specificConditions)) {
                        if (elseActions != null) {
                            validateElse(player, elseActions);
                        }
                        return false;
                    }
                    break;

                case "stats":
                    if (!validateStats(player, specificConditions)) {
                        if (elseActions != null) {
                            validateElse(player, elseActions);
                        }
                        return false;
                    }
                    break;
                case "permission":
                    if(!validatePermission(player, specificConditions)) {
                        if (elseActions != null) {
                            validateElse(player, elseActions);
                        }
                        return false;
                    }
                    break;
                case "citem":
                    if(!validateCitem(player, specificConditions)) {
                        if (elseActions != null) {
                            validateElse(player, elseActions);
                        }
                        return false;
                    }
                    break;
                case "currency":
                    if (!validateCurrency(player, specificConditions)) {
                        if (elseActions != null) {
                            validateElse(player, elseActions);
                        }
                        return false;
                    }
                    break;

                default:
                    throw new IllegalArgumentException("Unknown condition type: " + type);
            }
        }

        return true;
    }

    private void validateElse(Player p, JSONArray elseActions) {
        for (Object elseAction : elseActions) {
            String action = elseAction.toString();
            action = action.replace("%player%", p.getName());

            if (action.startsWith("send_message")) {
                String message = action.replace("send_message", "");
                String finalMessage = resolver.resolvePlaceholders(message, p);
                p.sendMessage(finalMessage.replace("&", "§"));
            }
            if (action.startsWith("close_gui")) {
                p.closeInventory();
            }
            if (action.startsWith("playsound")) {
                String[] parts = action.split(" ");
                if (parts.length > 1) {
                    String sound = parts[1];
                    p.playSound(p.getLocation(), sound, 1.0F, 1.0F);
                }
            }
            if (action.startsWith("player_cmd")) {
                String cmd = action.replace("player_cmd", "");
                plugin.getServer().dispatchCommand(p, cmd);
            } else {
                if (!action.startsWith("send_message") &&
                        !action.startsWith("close_gui") &&
                        !action.startsWith("playsound") &&
                        !action.startsWith("player_cmd")) {
                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), action);
                }
            }
        }
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
