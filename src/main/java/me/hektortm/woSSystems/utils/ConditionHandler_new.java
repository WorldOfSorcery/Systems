package me.hektortm.woSSystems.utils;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.systems.citems.CitemManager;
import me.hektortm.woSSystems.systems.stats.StatsManager;
import me.hektortm.woSSystems.systems.unlockables.UnlockableManager;
import me.hektortm.woSSystems.utils.dataclasses.Condition;
import org.bukkit.entity.Player;

import java.util.List;

public class ConditionHandler_new {
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final CitemManager citems = plugin.getCitemManager();
    private final UnlockableManager unlockables = plugin.getUnlockableManager();
    private final StatsManager stats = plugin.getStatsManager();


    public boolean checkConditions(Player player, List<Condition> conditions) {
        for (Condition condition : conditions) {
            if (!evaluate(player, condition)) {
                return false; // if any condition fails, deny
            }
        }
        return true;
    }

    private boolean evaluate(Player player, Condition condition) {
        switch (condition.getName().toLowerCase()) {
            case "has_citem":
                return citems.hasCitemAmount(player, condition.getValue(), Integer.parseInt(condition.getParameter())); // Placeholder until i have a correct method for it

            case "has_not_citem":
                return !citems.hasCitemAmount(player, condition.getValue(), Integer.parseInt(condition.getParameter()));

            case "has_unlockable":
                return unlockables.hasPlayerUnlockable(player, condition.getValue());

            case "has_not_unlockable":
                return !unlockables.hasPlayerUnlockable(player, condition.getValue());

            case "has_stats":
                return stats.hasStatValue(player, condition.getValue(), Integer.parseInt(condition.getParameter()));

            case "has_not_stats":
                return !stats.hasStatValue(player, condition.getValue(), Integer.parseInt(condition.getParameter()));

            case "permission":
                return player.hasPermission(condition.getValue());

            case "world":
                return player.getWorld().getName().equalsIgnoreCase(condition.getValue());

            // Add more condition types as needed
            default:
                return false;
        }
    }
}
