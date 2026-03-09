package me.hektortm.woSSystems.systems.citems.commands.subcommands;

import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collection;
import java.util.Map;

public class AttributeCmd extends SubCommand {

    @Override
    public String getName() {
        return "attribute";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.CITEM_ATTRIBUTE;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!PermissionUtil.isPlayer(sender)) return;

        Player p = (Player) sender;
        ItemStack item = p.getInventory().getItemInMainHand();

        if (item.getType() == Material.AIR) {
            Utils.error(p, "citems", "error.holding-item");
            return;
        }

        if (args.length < 1) {
            Utils.info(p, "citems", "info.usage.attribute");
            return;
        }

        ItemMeta meta = item.getItemMeta();

        switch (args[0].toLowerCase()) {
            case "add" -> {
                // /citem attribute add <attribute> <amount> [slot] [operation]
                if (args.length < 3) {
                    Utils.info(p, "citems", "info.usage.attribute-add");
                    return;
                }

                Attribute attribute = parseAttribute(args[1]);
                if (attribute == null) {
                    Utils.error(p, "citems", "error.attribute.invalid");
                    return;
                }

                double amount;
                try {
                    amount = Double.parseDouble(args[2]);
                } catch (NumberFormatException e) {
                    Utils.error(p, "citems", "error.attribute.invalid-amount");
                    return;
                }

                EquipmentSlotGroup slot = args.length >= 4 ? parseSlot(args[3]) : EquipmentSlotGroup.ANY;
                if (slot == null) {
                    Utils.error(p, "citems", "error.attribute.invalid-slot");
                    return;
                }

                AttributeModifier.Operation operation = args.length >= 5
                        ? parseOperation(args[4]) : AttributeModifier.Operation.ADD_NUMBER;
                if (operation == null) {
                    Utils.error(p, "citems", "error.attribute.invalid-operation");
                    return;
                }

                String keyName = "citem_" + attribute.getKey().getKey() + "_" + slot.toString().toLowerCase();
                AttributeModifier modifier = new AttributeModifier(
                        new NamespacedKey("wos", keyName),
                        amount,
                        operation,
                        slot
                );

                meta.addAttributeModifier(attribute, modifier);
                item.setItemMeta(meta);
                Utils.success(p, "citems", "attribute.added",
                        "%attribute%", attribute.getKey().getKey(),
                        "%amount%", String.valueOf(amount),
                        "%slot%", slot.toString());
            }
            case "remove" -> {
                // /citem attribute remove <attribute>
                if (args.length < 2) {
                    Utils.info(p, "citems", "info.usage.attribute-remove");
                    return;
                }

                Attribute attribute = parseAttribute(args[1]);
                if (attribute == null) {
                    Utils.error(p, "citems", "error.attribute.invalid");
                    return;
                }

                meta.removeAttributeModifier(attribute);
                item.setItemMeta(meta);
                Utils.success(p, "citems", "attribute.removed", "%attribute%", attribute.getKey().getKey());
            }
            case "clear" -> {
                meta.setAttributeModifiers(null);
                item.setItemMeta(meta);
                Utils.success(p, "citems", "attribute.cleared");
            }
            case "list" -> {
                Map<Attribute, Collection<AttributeModifier>> modifiers = meta.getAttributeModifiers();
                if (modifiers == null || modifiers.isEmpty()) {
                    Utils.info(p, "citems", "attribute.none");
                    return;
                }
                p.sendMessage("§aAttribute Modifiers:");
                for (Map.Entry<Attribute, Collection<AttributeModifier>> entry : modifiers.entrySet()) {
                    for (AttributeModifier mod : entry.getValue()) {
                        p.sendMessage("§7 - §e" + entry.getKey().getKey().getKey()
                                + " §f" + mod.getAmount()
                                + " §7(" + mod.getOperation().name() + ", " + mod.getSlotGroup() + ")");
                    }
                }
            }
            default -> Utils.info(p, "citems", "info.usage.attribute");
        }
    }

    private Attribute parseAttribute(String name) {
        return switch (name.toLowerCase()) {
            case "attack_damage", "damage" -> Attribute.ATTACK_DAMAGE;
            case "attack_speed", "speed" -> Attribute.ATTACK_SPEED;
            case "armor" -> Attribute.ARMOR;
            case "armor_toughness", "toughness" -> Attribute.ARMOR_TOUGHNESS;
            case "max_health", "health" -> Attribute.MAX_HEALTH;
            case "knockback_resistance", "knockback" -> Attribute.KNOCKBACK_RESISTANCE;
            case "movement_speed", "move_speed" -> Attribute.MOVEMENT_SPEED;
            case "luck" -> Attribute.LUCK;
            case "max_absorption", "absorption" -> Attribute.MAX_ABSORPTION;
            case "attack_knockback" -> Attribute.ATTACK_KNOCKBACK;
            case "attack_reach", "reach" -> Attribute.ENTITY_INTERACTION_RANGE;
            default -> null;
        };
    }

    private EquipmentSlotGroup parseSlot(String name) {
        return switch (name.toLowerCase()) {
            case "mainhand", "hand" -> EquipmentSlotGroup.MAINHAND;
            case "offhand" -> EquipmentSlotGroup.OFFHAND;
            case "head", "helmet" -> EquipmentSlotGroup.HEAD;
            case "chest", "chestplate" -> EquipmentSlotGroup.CHEST;
            case "legs", "leggings" -> EquipmentSlotGroup.LEGS;
            case "feet", "boots" -> EquipmentSlotGroup.FEET;
            case "armor" -> EquipmentSlotGroup.ARMOR;
            case "any" -> EquipmentSlotGroup.ANY;
            default -> null;
        };
    }

    private AttributeModifier.Operation parseOperation(String name) {
        return switch (name.toLowerCase()) {
            case "add", "add_number" -> AttributeModifier.Operation.ADD_NUMBER;
            case "multiply_base", "add_scalar" -> AttributeModifier.Operation.ADD_SCALAR;
            case "multiply_total", "multiply_scalar" -> AttributeModifier.Operation.MULTIPLY_SCALAR_1;
            default -> null;
        };
    }
}
