package me.hektortm.woSSystems.utils;

import me.hektortm.woSSystems.citems.DataManager;
import me.hektortm.woSSystems.stats.StatsManager;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlaceholderResolver {
    private final StatsManager statsManager;
    private final DataManager citemsManager;

    public PlaceholderResolver(StatsManager statsManager, DataManager citemsManager) {
        this.statsManager = statsManager;
        this.citemsManager = citemsManager;
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


        }
        return text;
    }

}
