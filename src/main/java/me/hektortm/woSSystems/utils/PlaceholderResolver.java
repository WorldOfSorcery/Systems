package me.hektortm.woSSystems.utils;

import me.hektortm.woSSystems.stats.StatsManager;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlaceholderResolver {
    private final StatsManager statsManager;

    public PlaceholderResolver(StatsManager statsManager) {
        this.statsManager = statsManager;
    }

    public String resolvePlaceholders(String text, Player player) {
        UUID playerUUID = player.getUniqueId();

        if(text.contains("{stats.")) {
            for (String statId : statsManager.getStats().keySet()) {
                String amountPlaceholder = "{stats." + statId + "_amount}";
                String maxPlaceholder = "{stats." + statId + "_max}";

                if (text.contains(amountPlaceholder)) {
                    long statAmount = statsManager.getPlayerStat(playerUUID, statId);
                    text = text.replace(amountPlaceholder, String.valueOf(statAmount));
                }

                if (text.contains(maxPlaceholder)) {
                    long statMax = statsManager.getStatMax(statId);
                    text = text.replace(maxPlaceholder, String.valueOf(statMax));
                }
            }
        }
        return text;
    }

}
