package me.hektortm.woSSystems.utils;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.DyedItemColor;
import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.systems.citems.CitemManager;
import me.hektortm.woSSystems.systems.stats.StatsManager;
import me.hektortm.woSSystems.utils.dataclasses.Constant;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.UUID;

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
                    long duration;
                    if (hub.getCooldownDAO().getRemainingSeconds(Bukkit.getOfflinePlayer(playerUUID), cooldownId) == null) {
                        duration = 0;
                    } else {
                        duration = hub.getCooldownDAO().getRemainingSeconds(Bukkit.getOfflinePlayer(playerUUID), cooldownId);
                    }
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
            for (String citemId : hub.getCitemDAO().getCitemIds()) {
                String namePlaceholder = "{citems.name:"+citemId+"}";
                String lorePlaceholder = "{citems.lore:"+citemId+"}";
                String materialPlaceholder = "{citems.material:"+citemId+"}";
                String modelPlaceholder = "{citems.model:"+citemId+"}";
                String tooltipPlaceholder = "{citems.tooltip:"+citemId+"}";

                ItemStack citem = hub.getCitemDAO().getCitem(citemId);
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
                if(text.contains(materialPlaceholder)) {
                    String material = citem.getType().toString();
                    text = text.replace(materialPlaceholder, material);
                }
                if(text.contains(modelPlaceholder)) {
                    NamespacedKey model = meta.getItemModel();
                    text = text.replace(modelPlaceholder, model.getKey() != null ? model.getKey() : "");
                }
                if(text.contains(tooltipPlaceholder)) {
                    NamespacedKey tooltip = meta.getTooltipStyle();
                    text = text.replace(tooltipPlaceholder, tooltip.getKey() != null ? tooltip.getKey() : "");
                }

            }

        }
        if (text.contains("{")) {
            for (Constant constant : hub.getConstantDAO().getAllConstants()) {
                String id = constant.getId();
                String value = constant.getValue();
                String placeholder = "{"+id+"}";
                text = text.replace(placeholder, value);
            }
        }
        return text;
    }

}
