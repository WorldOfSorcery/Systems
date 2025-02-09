package me.hektortm.woSSystems.systems.citems.commands.subcommands;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.systems.citems.CitemManager;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class FlagCommand extends SubCommand {
    private final CitemManager data;
    private final NamespacedKey undroppableKey;
    private final NamespacedKey unusableKey;
    private final NamespacedKey ownerKey;
    private NamespacedKey placeableKey;

    public FlagCommand(CitemManager data) {
        this.data = data;
        placeableKey = data.getPlaceableKey();
        ownerKey = new NamespacedKey(WoSSystems.getPlugin(WoSSystems.class), "owner");
        undroppableKey = new NamespacedKey(Bukkit.getPluginManager().getPlugin("WoSSystems"), "undroppable");
        unusableKey = new NamespacedKey(Bukkit.getPluginManager().getPlugin("WoSSystems"), "unusable");
    }

    @Override
    public String getName() {
        return "flag";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.CITEM_FLAGS;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!PermissionUtil.isPlayer(sender)) return;

        Player p = (Player) sender;

        ItemStack itemInHand = p.getInventory().getItemInMainHand();
        ItemMeta meta = itemInHand.getItemMeta();

        if (args.length < 2) {
            Utils.error(p, "citems", "error.usage.flag");
            return;
        }

        if(!data.getErrorHandler().handleCitemErrors(itemInHand, p)) return;

        String flagCmd = args[0];
        String flag = args[1].toLowerCase();
        switch (flagCmd.toLowerCase()) {
            case "add":
                if (flag.equals("undroppable")) {
                    PersistentDataContainer data = meta.getPersistentDataContainer();
                    data.set(undroppableKey, PersistentDataType.BOOLEAN, true);
                    Utils.successMsg(p, "citems", "flag.add.undroppable");
                }
                if (flag.equals("unbreakable")) {
                    meta.setUnbreakable(true);
                    Utils.successMsg(p, "citems", "flag.add.unbreakable");
                }
                if (flag.equals("unusable")) {
                    PersistentDataContainer data = meta.getPersistentDataContainer();
                    data.set(unusableKey, PersistentDataType.BOOLEAN, true);
                    Utils.successMsg(p, "citems", "flag.add.unusable");
                }
                if (flag.equals("hide")) {

                    // TODO: Fix item Attributes showing

                    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
                    meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
                    meta.addItemFlags(ItemFlag.HIDE_DYE);
                    meta.addItemFlags(ItemFlag.HIDE_ARMOR_TRIM);
                    meta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);
                    meta.addItemFlags(ItemFlag.HIDE_DESTROYS);
                    meta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
                    Utils.successMsg(p, "citems", "flag.add.hide");
                }
                if (flag.equals("owner")) {
                    PersistentDataContainer data = meta.getPersistentDataContainer();
                    data.set(ownerKey, PersistentDataType.STRING, "");
                }
                if(flag.equals("placeable")) {
                    if (args.length < 3) {
                        p.sendMessage("not enough arguments"); //TODO lang
                        return;
                    }
                    String parameter = args[2];
                    PersistentDataContainer data = meta.getPersistentDataContainer();

                    if (parameter.equals("small")) {
                        data.set(placeableKey, PersistentDataType.INTEGER, 1);
                    } else if (parameter.equals("normal")) {
                        data.set(placeableKey, PersistentDataType.INTEGER, 2);
                    } else {
                        p.sendMessage("Use: /citem flag add placeable <small/normal>");
                        return;
                    }

                    Utils.success(p, "citems", "flag.add.placeable");
                }

                break;

            case "remove":
                if (flag.equals("undroppable")) {
                    PersistentDataContainer data = meta.getPersistentDataContainer();
                    data.remove(undroppableKey);
                    Utils.success(p, "citems", "flag.remove.undroppable");
                }
                if (flag.equals("unbreakable")) {
                    meta.setUnbreakable(false);
                    Utils.success(p, "citems", "flag.remove.unbreakable");
                }
                if (flag.equals("unusable")) {
                    PersistentDataContainer data = meta.getPersistentDataContainer();
                    data.remove(unusableKey);
                    Utils.success(p, "citems", "flag.remove.unusable");
                }
                if (flag.equals("hide"))
                {   //noinspection removal
                    AttributeModifier fakeArmorToughness = new AttributeModifier(
                            UUID.randomUUID(), "fake_armor_toughness", 0, AttributeModifier.Operation.ADD_SCALAR);

                    meta.addAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS, fakeArmorToughness);
                    meta.removeItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
                    meta.removeItemFlags(ItemFlag.HIDE_UNBREAKABLE);
                    meta.removeItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
                    meta.removeItemFlags(ItemFlag.HIDE_DYE);
                    meta.removeItemFlags(ItemFlag.HIDE_ARMOR_TRIM);
                    meta.removeItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);
                    meta.removeItemFlags(ItemFlag.HIDE_DESTROYS);
                    meta.removeItemFlags(ItemFlag.HIDE_PLACED_ON);
                    Utils.success(p, "citems", "flag.remove.hide");
                }
                if (flag.equals("placeable")) {
                    PersistentDataContainer data = meta.getPersistentDataContainer();
                    data.remove(placeableKey);
                    Utils.success(p, "citems", "flag.remove.placeable");
                }
                break;

            default:
                Utils.error(p, "citems", "error.usage.flag");
                return;
        }
        itemInHand.setItemMeta(meta);
    }
}
