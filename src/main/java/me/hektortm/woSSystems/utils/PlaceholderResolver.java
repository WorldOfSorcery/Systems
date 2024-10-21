package me.hektortm.woSSystems.utils;

import me.hektortm.woSSystems.systems.citems.DataManager;
import me.hektortm.woSSystems.systems.stats.StatsManager;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlaceholderResolver {
    private final StatsManager statsManager;
    private final DataManager citemsManager;

    public PlaceholderResolver(StatsManager statsManager, DataManager citemsManager) {
        this.statsManager = statsManager;
        this.citemsManager = citemsManager;
    }

    // TODO UPDATE WHEN INVENTORY IS OPENED AGAIN

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
        if (text.contains("{global_stats.")) {
            for (String statId : statsManager.getGlobalStats().keySet()) {
                String amountPlaceholder = "{global_stats." + statId + "_amount}";
                String maxPlaceholder = "{global_stats." + statId + "_max}";

                if (text.contains(amountPlaceholder)) {
                    long statAmount = statsManager.getGlobalStat(statId);
                    text = text.replace(amountPlaceholder, String.valueOf(statAmount));
                }
                if (text.contains(maxPlaceholder)) {
                    long statMax = statsManager.getGlobalStatMax(statId);
                    text = text.replace(maxPlaceholder, String.valueOf(statMax));
                }
            }
        }
        if(text.contains("{citems.")) {
            // TODO
            for (String citemId : statsManager.getStats().keySet()) {
                String namePlaceholder = "{citems."+citemId+"_name}";
                String lorePlaceholder = "{citems."+citemId+"_lore}";

                if (text.contains(namePlaceholder)) {
                    String name = "PLACEHOLDER";
                    text = text.replace(namePlaceholder, name);
                }
                if (text.contains(lorePlaceholder)) {
                    String lore = "PLACEHOLDER";
                    text = text.replace(lorePlaceholder, lore);
                }

            }
        }
        return text;
    }

}
