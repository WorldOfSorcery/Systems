package me.hektortm.woSSystems.utils;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.systems.citems.CitemManager;
import me.hektortm.woSSystems.systems.stats.StatsManager;
import me.hektortm.woSSystems.utils.model.Constant;
import me.hektortm.woSSystems.utils.types.CosmeticType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

import static java.lang.Long.parseLong;

/**
 * Resolves placeholder tokens embedded in strings to their live runtime values.
 *
 * <p>The primary entry point is {@link #resolvePlaceholders(String, Player)},
 * which accepts a raw placeholder string (without surrounding braces) and
 * returns the resolved text.  Tokens follow the syntax
 * {@code namespace.key:id}, where {@code namespace} selects the data source
 * and {@code key} / {@code id} narrow down the specific value:</p>
 *
 * <ul>
 *   <li>{@code player_name} / {@code player_nick} — player display name or nickname</li>
 *   <li>{@code player_cosmetic.&lt;type&gt;} — currently equipped cosmetic display string</li>
 *   <li>{@code stats.amount:&lt;id&gt;} / {@code stats.max:&lt;id&gt;} — player stat values</li>
 *   <li>{@code global_stats.amount:&lt;id&gt;} / {@code global_stats.max:&lt;id&gt;} — global stat values</li>
 *   <li>{@code cooldowns.duration:&lt;id&gt;} — remaining cooldown time formatted as {@code HH:MM:SS}</li>
 *   <li>{@code citems.name|lore|material|model|tooltip:&lt;id&gt;} — custom item metadata</li>
 *   <li>bare token without a dot — looked up as a {@link Constant} by ID</li>
 * </ul>
 *
 * <p>Unrecognised namespaces return the original token wrapped in braces.</p>
 */
public class PlaceholderResolver {
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final DAOHub hub;
    private final StatsManager statsManager = plugin.getStatsManager();
    private final CitemManager citemsManager = plugin.getCitemManager();

    /**
     * @param hub the DAO hub used to access all data sources
     */
    public PlaceholderResolver(DAOHub hub) {
        this.hub = hub;
    }

    // TODO UPDATE WHEN INVENTORY IS OPENED AGAIN

    /**
     * Resolves a single placeholder token to its runtime string value.
     *
     * @param raw    the placeholder token (without enclosing braces), e.g.
     *               {@code "stats.amount:kills"} or {@code "player_name"}
     * @param player the player whose data is used for player-scoped placeholders
     * @return the resolved string, or the original token in braces if unknown
     */
    public String resolvePlaceholders(String raw, Player player) {
        UUID uuid = player.getUniqueId();

        // player_name
        if (!raw.contains(".")) {
            return switch (raw) {
                case "player_name" -> player.getName();
                case "player_nick" -> hub.getNicknameDAO().getNickname(uuid);
                default -> resolveConstant(raw);
            };
        }

        // namespace.key:ID
        String[] dotSplit = raw.split("\\.", 2);
        String namespace = dotSplit[0];
        String rest = dotSplit[1];

        String key;
        String id = null;

        if (rest.contains(":")) {
            String[] colonSplit = rest.split(":", 2);
            key = colonSplit[0];
            id = colonSplit[1];
        } else {
            key = rest;
        }

        return switch (namespace) {
            case "player_cosmetic" -> resolveCosmetic(key, player);
            case "stats" -> resolveStats(key, id, uuid);
            case "global_stats" -> resolveGlobalStats(key, id);
            case "cooldowns" -> resolveCooldown(key, id, uuid);
            case "citems" -> resolveCitem(key, id);
            default -> "{" + raw + "}";
        };
    }

    private String resolveConstant(String id) {
        Constant constant = hub.getConstantDAO().getConstant(id);
        return constant == null ? "" : constant.getValue();
    }

    private String resolveCitem(String key, String id) {
        if (id == null) return "";

        ItemStack item = hub.getCitemDAO().getCitem(id);
        if (item == null) return "";

        ItemMeta meta = item.getItemMeta();

        return switch (key) {
            case "name" -> meta.getDisplayName();
            case "lore" -> meta.getLore() == null ? "" : String.join("\n", meta.getLore());
            case "material" -> item.getType().toString();
            case "model" -> meta.getItemModel() != null ? meta.getItemModel().getKey() : "";
            case "tooltip" -> meta.getTooltipStyle() != null ? meta.getTooltipStyle().getKey() : "";
            default -> "";
        };
    }

    private String resolveCooldown(String key, String id, UUID uuid) {
        if (!"duration".equals(key) || id == null) return "";

        Long seconds = hub.getCooldownDAO()
                .getRemainingSeconds(Bukkit.getOfflinePlayer(uuid), id);

        return seconds == null
                ? "00:00:00"
                : Parsers.formatCooldownTime(seconds);
    }

    private String resolveGlobalStats(String key, String id) {
        if (id == null) return "";

        return switch (key) {
            case "amount" -> String.valueOf(
                    statsManager.getGlobalStatValue(id)
            );
            case "max" -> String.valueOf(
                    statsManager.getGlobalStatMax(id)
            );
            default -> "";
        };
    }

    private String resolveCosmetic(String key, Player player) {
        try {
            CosmeticType type = CosmeticType.valueOf(key.toUpperCase());
            return hub.getCosmeticsDAO().getCurrentCosmetic(player, type);
        } catch (IllegalArgumentException e) {
            return "";
        }
    }

    private String resolveStats(String key, String id, UUID uuid) {
        if (id == null) return "";

        return switch (key) {
            case "amount" -> String.valueOf(
                    statsManager.getPlayerStat(uuid, id)
            );
            case "max" -> String.valueOf(
                    statsManager.getStatMax(id)
            );
            default -> "";
        };
    }

}
