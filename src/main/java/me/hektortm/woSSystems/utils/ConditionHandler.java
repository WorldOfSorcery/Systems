package me.hektortm.woSSystems.utils;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.economy.EcoManager;
import me.hektortm.woSSystems.systems.citems.CitemManager;
import me.hektortm.woSSystems.systems.stats.StatsManager;
import me.hektortm.woSSystems.systems.unlockables.UnlockableManager;
import me.hektortm.woSSystems.utils.dataclasses.ParticleData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConditionHandler {

    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final UnlockableManager unlockableManager;
    private final StatsManager statsManager;
    private final EcoManager ecoManager;
    private final CitemManager citemManager;
    private PlaceholderResolver resolver;
    private final Parsers parsers = new Parsers();

    public ConditionHandler(UnlockableManager unlockableManager, StatsManager statsManager, EcoManager ecoManager, CitemManager citemManager) {
        this.unlockableManager = unlockableManager;
        this.statsManager = statsManager;
        this.ecoManager = ecoManager;
        this.citemManager = citemManager;
        resolver  = new PlaceholderResolver(statsManager, citemManager);
    }

    public ConditionOutcomes getUnmetConditionOutcomes(Player player, JSONArray conditions) {
        ConditionOutcomes outcomes = new ConditionOutcomes();

        if (conditions == null) {
            return outcomes; // No conditions mean nothing to check
        }

        for (Object obj : conditions) {
            JSONObject conditionPair = (JSONObject) obj;

            // There should only be one key per conditionPair
            String conditionKey = (String) conditionPair.keySet().iterator().next();
            JSONObject conditionData = (JSONObject) conditionPair.get(conditionKey);

            // Split the condition key into type and specific identifier
            String[] parts = conditionKey.split(":");
            String conditionType = parts[0];
            String conditionIdentifier = parts.length > 1 ? parts[1] : null;

            // Evaluate the condition based on its type
            boolean isMet = evaluateCondition(player, conditionType, conditionIdentifier, conditionData);

            if (!isMet) {
                // If the condition is not met, process only the "false" outcomes and stop further checks
                JSONObject falseOutcome = (JSONObject) conditionData.get("false");

                if (falseOutcome != null) {
                    if (falseOutcome.containsKey("actions")) {
                        JSONArray actions = (JSONArray) falseOutcome.get("actions");
                        for (Object actionObj : actions) {
                            outcomes.actions.add(actionObj.toString());
                        }
                    }

                    if (falseOutcome.containsKey("hologram")) {
                        JSONArray hologram = (JSONArray) falseOutcome.get("hologram");
                        for (Object lineObj : hologram) {
                            outcomes.holograms.add(lineObj.toString());
                        }
                    }

                    if (falseOutcome.containsKey("particles")) {
                        JSONObject particles = (JSONObject) falseOutcome.get("particles");
                        if (particles != null && !particles.isEmpty()) {
                            String type = (String) particles.get("type");
                            String color = (String) particles.get("color");
                            outcomes.setParticleType(type);
                            outcomes.setParticleColor(color);
                        }

                    }
                }
                return outcomes; // Stop processing further conditions
            }

            // If the condition is met, process the "true" outcomes
            JSONObject trueOutcome = (JSONObject) conditionData.get("true");
            if (trueOutcome != null) {
                if (trueOutcome.containsKey("actions")) {
                    JSONArray actions = (JSONArray) trueOutcome.get("actions");
                    for (Object actionObj : actions) {
                        outcomes.actions.add(actionObj.toString());
                    }
                }

                if (trueOutcome.containsKey("hologram")) {
                    JSONArray hologram = (JSONArray) trueOutcome.get("hologram");
                    for (Object lineObj : hologram) {
                        String objString = lineObj.toString();
                        String finalString = lineObj.toString();
                        if (objString.contains("<unicode>")) {
                            String colorCode = "";
                            if (objString.startsWith("§") && objString.length() > 1) {
                                colorCode = objString.substring(0, 2);
                            }

                            String toParse = objString.substring(objString.indexOf("<unicode>") + "<unicode>".length());
                            String parsedUnicode = Parsers.parseUniStatic(toParse);
                            finalString = colorCode + parsedUnicode;
                        }
                        if (objString.contains(":")) {
                            String[] objParts = objString.split(":");
                            String object = objParts[0];
                            String value = objParts[1];
                            switch (object) {
                                case "icon":
                                    String icon = Icons.getIconByName(value);
                                    if (icon == null) {
                                        finalString = "§cUNKNOWN ICON: '" + value+"'";
                                    } else {
                                        finalString = "§f" + icon;
                                    }
                                    break;
                                default:
                                    finalString = objString;
                            }
                        }
                        outcomes.holograms.add(finalString);
                    }
                }

                if (trueOutcome.containsKey("particles")) {
                    JSONObject particles = (JSONObject) trueOutcome.get("particles");
                    if (particles != null && !particles.isEmpty()) {
                        String type = (String) particles.get("type");
                        String color = (String) particles.get("color");
                        outcomes.setParticleType(type);
                        outcomes.setParticleColor(color);
                    }

                }
            }

        }

        return outcomes; // Return outcomes if all conditions are met
    }

    public boolean validateConditionsBOOL(Player player, JSONArray conditions) {
        boolean validation = false;
        if (conditions == null) {
            return true; // No conditions mean nothing to check
        }

        for (Object obj : conditions) {
            JSONObject conditionPair = (JSONObject) obj;

            // There should only be one key per conditionPair
            String conditionKey = (String) conditionPair.keySet().iterator().next();
            JSONObject conditionData = (JSONObject) conditionPair.get(conditionKey);

            // Split the condition key into type and specific identifier
            String[] parts = conditionKey.split(":");
            String conditionType = parts[0];
            String conditionIdentifier = parts.length > 1 ? parts[1] : null;

            // Evaluate the condition based on its type
            boolean isMet = evaluateCondition(player, conditionType, conditionIdentifier, conditionData);

            if (!isMet) {
                return false; // Stop processing further conditions
            }

            validation = true;


        }

        return validation; // Return outcomes if all conditions are met
    }

    public boolean evaluateCondition(Player player, String conditionType, String conditionIdentifier, JSONObject conditionData) {
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

    public static class ConditionOutcomes {
        public final List<String> actions = new ArrayList<>();
        public final List<String> holograms = new ArrayList<>();
        private String particleType;
        private String particleColor;

        // Getter for particleType
        public String getParticleType() {
            return particleType;
        }

        // Getter for particleColor
        public String getParticleColor() {
            return particleColor;
        }

        // Setter for particleType
        public void setParticleType(String particleType) {
            this.particleType = particleType;
        }

        // Setter for particleColor
        public void setParticleColor(String particleColor) {
            this.particleColor = particleColor;
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
