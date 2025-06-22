package me.hektortm.woSSystems.utils;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.systems.citems.CitemManager;
import me.hektortm.woSSystems.systems.stats.StatsManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import static java.lang.Long.parseLong;

public class PlaceholderResolver {
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final DAOHub hub;
    private final StatsManager statsManager = plugin.getStatsManager();
    private final CitemManager citemsManager = plugin.getCitemManager();

    public PlaceholderResolver(DAOHub hub) {
        this.hub = hub;
    }

    // TODO UPDATE WHEN INVENTORY IS OPENED AGAIN

    public String resolvePlaceholders(String text, Player player) {
        UUID playerUUID = player.getUniqueId();


        if(text.contains("{stats.")) {
            for (String statId : statsManager.getStats().keySet()) {
                String amountPlaceholder = "{stats.amount:" + statId +"}";
                String maxPlaceholder = "{stats.max:" + statId + "}";

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
                String amountPlaceholder = "{global_stats.amount:" + statId + "}";
                String maxPlaceholder = "{global_stats.max:" + statId + "}";

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
        if (text.contains("{cooldowns.")) {
            for (String cooldownId : hub.getCooldownDAO().getAllCooldowns().keySet()) {
                String durationPlaceholder = "{cooldowns.duration:" + cooldownId + "}";

                if (text.contains(durationPlaceholder)) {
                    long duration = hub.getCooldownDAO().getRemainingSeconds(Bukkit.getOfflinePlayer(playerUUID), cooldownId);
                    // Convert duration from seconds to a human-readable format
                    String formattedDuration = Parsers.formatCooldownTime(duration);
                    text = text.replace(durationPlaceholder, formattedDuration);
                    if (hub.getCooldownDAO().getRemainingSeconds(Bukkit.getOfflinePlayer(playerUUID), cooldownId) == null) {
                        // If the cooldown is expired, replace with "Expired"
                        text = text.replace(durationPlaceholder, "00:00:00");
                    }
                }
            }
        }
        if(text.contains("{citems.")) {
            for (String citemId : citemsManager.getCitemDAO().getCitemIds()) {
                String namePlaceholder = "{citems.name:"+citemId+"}";
                String lorePlaceholder = "{citems.lore:"+citemId+"}";

                ItemStack citem = citemsManager.getCitemDAO().getCitem(citemId);
                ItemMeta meta = citem.getItemMeta();
                if (text.contains(namePlaceholder)) {
                    String name = meta.getDisplayName();
                    text = text.replace(namePlaceholder, name);
                }
                if (text.contains(lorePlaceholder)) {
                    List<String> lore = meta.getLore();
                    StringBuilder builder = new StringBuilder();
                    for ( int i = 0; i < lore.size(); i++ ) {
                        if (i != lore.size()-1) {
                            builder.append(lore.get(i)+"\n");
                        } else {
                            builder.append(lore.get(i));
                        }

                    }
                    text = text.replace(lorePlaceholder, builder.toString());
                }

            }

        }
        return text;
    }

}
