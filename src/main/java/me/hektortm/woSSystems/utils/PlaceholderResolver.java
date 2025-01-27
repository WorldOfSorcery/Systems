package me.hektortm.woSSystems.utils;

import me.hektortm.woSSystems.systems.citems.CitemManager;
import me.hektortm.woSSystems.systems.stats.StatsManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.List;
import java.util.UUID;

public class PlaceholderResolver {
    private final StatsManager statsManager;
    private final CitemManager citemsManager;

    public PlaceholderResolver(StatsManager statsManager, CitemManager citemsManager) {
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
                    long statAmount = statsManager.getGlobalStatValue(statId);
                    text = text.replace(amountPlaceholder, String.valueOf(statAmount));
                }
                if (text.contains(maxPlaceholder)) {
                    long statMax = statsManager.getGlobalStatMax(statId);
                    text = text.replace(maxPlaceholder, String.valueOf(statMax));
                }
            }
        }
        if(text.contains("{citems.")) {
            // BIG TODO
            /*
            for (File file : citemsManager.citemFolder.listFiles()) {
                String id = file.getName().replace(".json", "");
                String namePlaceholder = "{citems."+id+"_name}";
                String lorePlaceholder = "{citems."+id+"_lore}";

                ItemStack citem = citemsManager.loadItemFromFile(file);
                ItemMeta meta = citem.getItemMeta();
                if (text.contains(namePlaceholder)) {
                    String name = meta.getDisplayName();
                    text = text.replace(namePlaceholder, name);
                }
                if (text.contains(lorePlaceholder)) {
                    List<String> lore = meta.getLore();
                    StringBuilder builder = new StringBuilder();
                    for ( int i = 0; i < lore.size(); i++ ) {
                        builder.append(lore.get(i));
                    }
                    text = text.replace(lorePlaceholder, builder.toString());
                }

            }
             */
        }
        return text;
    }

}
