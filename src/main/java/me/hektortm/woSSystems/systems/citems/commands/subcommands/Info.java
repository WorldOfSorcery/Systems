package me.hektortm.woSSystems.systems.citems.commands.subcommands;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.papermc.paper.datacomponent.DataComponentTypes;
import me.hektortm.woSSystems.utils.Keys;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.Rarities;
import me.hektortm.woSSystems.utils.SubCommand;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collection;
import java.util.Map;

public class Info extends SubCommand {

    @Override
    public String getName() {
        return "info";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.CITEM_INFO;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!PermissionUtil.isPlayer(sender)) return;
        Player p = (Player) sender;
        ItemStack item = p.getInventory().getItemInMainHand();

        if (item.getType() == Material.AIR) {
            p.sendMessage("§cYou must be holding an item.");
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            p.sendMessage("§cThis item has no metadata.");
            return;
        }

        PersistentDataContainer data = meta.getPersistentDataContainer();
        String itemId = data.get(Keys.ID.get(), PersistentDataType.STRING);

        p.sendMessage("§8§m                    §r §aCitem Info §8§m                    ");
        p.sendMessage("§7ID: §f" + (itemId != null ? itemId : "§cNot a saved Citem"));
        p.sendMessage("§7Material: §f" + item.getType().name());

        // Custom Rarity
        String rarityRaw = data.get(Keys.CUSTOM_RARITY.get(), PersistentDataType.STRING);
        if (rarityRaw != null) {
            Rarities rarity = Rarities.fromName(rarityRaw);
            if (rarity != null) {
                p.sendMessage("§7Rarity: " + rarity.getLoreLine());
            }
        }

        // Max stack size
        Integer maxStack = item.getData(DataComponentTypes.MAX_STACK_SIZE);
        if (maxStack != null && maxStack != item.getType().getMaxStackSize()) {
            p.sendMessage("§7Max Stack: §f" + maxStack);
        }

        // Fire resistant
        boolean fireResistant = item.hasData(DataComponentTypes.FIRE_RESISTANT);
        if (fireResistant) {
            p.sendMessage("§7Fire Resistant: §atrue");
        }

        // Food
        if (meta.hasFood()) {
            var food = meta.getFood();
            p.sendMessage("§7Food: §fNutrition=" + food.getNutrition()
                    + " Saturation=" + food.getSaturation()
                    + " AlwaysEat=" + food.canAlwaysEat());
        }

        // Flags
        boolean undroppable = data.has(Keys.UNDROPPABLE.get(), PersistentDataType.BOOLEAN);
        boolean unusable = data.has(Keys.UNUSABLE.get(), PersistentDataType.BOOLEAN);
        boolean unbreakable = meta.isUnbreakable();
        boolean hasHideFlags = !meta.getItemFlags().isEmpty();
        Integer placeableType = data.get(Keys.PLACEABLE.get(), PersistentDataType.INTEGER);

        p.sendMessage("§7Flags:");
        if (undroppable) p.sendMessage("  §e- Undroppable");
        if (unusable) p.sendMessage("  §e- Unusable");
        if (unbreakable) p.sendMessage("  §e- Unbreakable");
        if (hasHideFlags) p.sendMessage("  §e- Hide Attributes");
        if (fireResistant) p.sendMessage("  §e- Fire Resistant");
        if (placeableType != null) p.sendMessage("  §e- Placeable §f(" + (placeableType == 1 ? "small" : "normal") + ")");

        // Model / Tooltip
        NamespacedKey modelKey = meta.getItemModel();
        NamespacedKey tooltip = meta.getTooltipStyle();
        boolean hiddenTooltip = meta.isHideTooltip();
        p.sendMessage("§7Model: §f" + (modelKey != null ? modelKey.toString() : "§7none"));
        p.sendMessage("§7Tooltip Style: §f" + (tooltip != null ? tooltip.toString() : "§7none"));
        if (hiddenTooltip) p.sendMessage("§7Hidden Tooltip: §ctrue");

        // Actions
        String leftAction = data.get(Keys.LEFT_ACTION.get(), PersistentDataType.STRING);
        String rightAction = data.get(Keys.RIGHT_ACTION.get(), PersistentDataType.STRING);
        String placedAction = data.get(Keys.PLACED_ACTION.get(), PersistentDataType.STRING);

        p.sendMessage("§7Actions:");
        p.sendMessage("  §7Left: §f" + (leftAction != null ? leftAction : "§7none"));
        p.sendMessage("  §7Right: §f" + (rightAction != null ? rightAction : "§7none"));
        p.sendMessage("  §7Placed: §f" + (placedAction != null ? placedAction : "§7none"));

        // Attribute Modifiers
        Map<Attribute, Collection<AttributeModifier>> attrMods = meta.getAttributeModifiers();
        if (attrMods != null && !attrMods.isEmpty()) {
            p.sendMessage("§7Attributes:");
            for (Map.Entry<Attribute, Collection<AttributeModifier>> entry : attrMods.entrySet()) {
                for (AttributeModifier mod : entry.getValue()) {
                    p.sendMessage("  §e" + entry.getKey().getKey().getKey()
                            + " §f" + (mod.getAmount() >= 0 ? "+" : "") + mod.getAmount()
                            + " §7[" + mod.getOperation().name() + ", " + mod.getSlotGroup() + "]");
                }
            }
        }

        // Dynamic lore
        String dynJson = data.get(Keys.DYNAMIC_LORE.get(), PersistentDataType.STRING);
        if (dynJson != null) {
            try {
                JsonArray entries = JsonParser.parseString(dynJson).getAsJsonArray();
                p.sendMessage("§7Dynamic Lore §7(" + entries.size() + " entries):");
                for (int i = 0; i < entries.size(); i++) {
                    JsonObject entry = entries.get(i).getAsJsonObject();
                    String type = entry.has("type") ? entry.get("type").getAsString() : "perm";
                    String text = entry.get("text").getAsString();
                    String info = switch (type) {
                        case "perm" -> "§6[perm] §e" + (entry.has("perm") ? entry.get("perm").getAsString() : "?");
                        case "condition" -> "§d[cond] §e" + entry.get("condition").getAsString()
                                + " §7v=" + entry.get("value").getAsString()
                                + " p=" + entry.get("parameter").getAsString();
                        default -> "§7[" + type + "]";
                    };
                    p.sendMessage("  §7[" + i + "] " + info + " §7→ §f" + text);
                }
            } catch (Exception ignored) {}
        }

        p.sendMessage("§8§m                                                    ");
    }
}
