package me.hektortm.woSSystems.utils;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.systems.citems.CitemManager;
import me.hektortm.woSSystems.systems.cooldowns.CooldownManager;
import me.hektortm.woSSystems.systems.stats.StatsManager;
import me.hektortm.woSSystems.systems.unlockables.UnlockableManager;
import me.hektortm.woSSystems.utils.dataclasses.Condition;
import me.hektortm.wosCore.discord.DiscordLog;
import me.hektortm.wosCore.discord.DiscordLogger;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class ConditionHandler {
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final CitemManager citems = plugin.getCitemManager();
    private final UnlockableManager unlockables = plugin.getUnlockableManager();
    private final StatsManager stats = plugin.getStatsManager();
    private final CooldownManager cooldowns = plugin.getCooldownManager();
    private final DAOHub hub;

    public ConditionHandler(DAOHub hub) {
        this.hub = hub;
    }


    public boolean checkConditions(Player player, List<Condition> conditions) {
        if (conditions == null || conditions.isEmpty()) return true;
        for (Condition condition : conditions) {
            if (!evaluate(player, condition)) return false;
        }
        return true;
    }

    public boolean evaluate(Player player, Condition condition) {
        try {
            switch (condition.getName().toLowerCase()) {
                case "has_citem": return citems.hasCitemAmount(player, condition.getValue(), Integer.parseInt(condition.getParameter())); // Placeholder until i have a correct method for it
                case "has_not_citem": return !citems.hasCitemAmount(player, condition.getValue(), Integer.parseInt(condition.getParameter()));

                case "has_unlockable": return unlockables.hasPlayerUnlockable(player, condition.getValue());
                case "has_not_unlockable": return !unlockables.hasPlayerUnlockable(player, condition.getValue());

                case "has_stats_greater_than": return stats.getPlayerStat(player.getUniqueId(), condition.getValue()) > Long.parseLong(condition.getParameter());
                case "has_stats_less_than": return stats.getPlayerStat(player.getUniqueId(), condition.getValue()) < Long.parseLong(condition.getParameter());
                case "has_stats_equal_to": return stats.getPlayerStat(player.getUniqueId(), condition.getValue()) == Long.parseLong(condition.getParameter());

                case "global_stats_greater_than": return stats.getGlobalStatValue(condition.getValue()) > Long.parseLong(condition.getParameter());
                case "global_stats_less_than": return stats.getGlobalStatValue(condition.getValue()) < Long.parseLong(condition.getParameter());
                case "global_stats_equal_to": return  stats.getGlobalStatValue(condition.getValue()) == Long.parseLong(condition.getParameter());

                case "is_in_region":
                    Map<UUID, String> playerRegions = plugin.getPlayerRegions();
                    String regionId = playerRegions.get(player.getUniqueId());
                    return regionId != null && regionId.equalsIgnoreCase(condition.getValue());
                case "is_not_in_region":
                    Map<UUID, String> playerRegionsNot = plugin.getPlayerRegions();
                    String regionIdNot = playerRegionsNot.get(player.getUniqueId());
                    return regionIdNot == null || !regionIdNot.equalsIgnoreCase(condition.getValue());

                    case "has_active_cooldown": return isCooldownActive(player, condition.getValue());
                case "has_not_active_cooldown": return !isCooldownActive(player, condition.getValue());

                case "has_badge": return hub.getCosmeticsDAO().hasCosmetic(player.getUniqueId(), CosmeticType.BADGE, condition.getValue());
                case "has_not_badge": return !hub.getCosmeticsDAO().hasCosmetic(player.getUniqueId(), CosmeticType.BADGE, condition.getValue());
                case "has_prefix": return hub.getCosmeticsDAO().hasCosmetic(player.getUniqueId(), CosmeticType.PREFIX, condition.getValue());
                case "has_not_prefix": return !hub.getCosmeticsDAO().hasCosmetic(player.getUniqueId(), CosmeticType.PREFIX, condition.getValue());
                case "has_title": return hub.getCosmeticsDAO().hasCosmetic(player.getUniqueId(), CosmeticType.TITLE, condition.getValue());
                case "has_not_title": return !hub.getCosmeticsDAO().hasCosmetic(player.getUniqueId(), CosmeticType.TITLE, condition.getValue());

                case "has_currency": return hub.getEconomyDAO().getPlayerCurrency(player, condition.getValue()) >= (condition.getParameter() != null ? Long.parseLong(condition.getParameter()) : 0);
                case "has_not_currency": return hub.getEconomyDAO().getPlayerCurrency(player, condition.getValue()) <= (condition.getParameter() != null ? Long.parseLong(condition.getParameter()) : 0);
                case "has_permission": return player.hasPermission(condition.getValue());

                case "in_world": return player.getWorld().getName().equalsIgnoreCase(condition.getValue());

                default: return false;
            }
        } catch (Exception e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE,
                    plugin,
                    "CH:61ed507d",
                    "Error evaluating condition: " + condition.getName() + " for player: " + player.getName() + ". Error: ", e
                    ));
            plugin.writeLog("ConditionHandler", Level.SEVERE, "Error evaluating condition: " + condition.getName() + " for player: " + player.getName() + ". Error: " + e.getMessage());
            return false;
        }

    }

    public boolean isCooldownActive(OfflinePlayer oP, String cooldownId) {
        return hub.getCooldownDAO().getRemainingSeconds(oP, cooldownId) != null;
    }

}
