package me.hektortm.woSSystems.systems.citems.commands.subcommands;

import me.hektortm.woSSystems.utils.Keys;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class  Flag extends SubCommand {

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

        ItemStack item = p.getInventory().getItemInMainHand();

        if (item.getType() == Material.AIR) {
            Utils.error(p, "citems", "error.holding-item");
            return;
        }

        ItemMeta meta = item.getItemMeta();

        if (args.length < 2) {
            Utils.info(p, "citems", "info.usage.flag");
            return;
        }


        String flagCmd = args[0];
        String flag = args[1].toLowerCase();
        PersistentDataContainer data = meta.getPersistentDataContainer();
        switch (flagCmd.toLowerCase()) {
            case "add":
                switch (flag) {
                    case "undroppable" -> {
                        data.set(Keys.UNDROPPABLE.get(), PersistentDataType.BOOLEAN, true);
                        Utils.success(p, "citems", "flag.add", "%flag%", flag);
                    }
                    case "unbreakable" -> {
                        meta.setUnbreakable(true);
                        Utils.success(p, "citems", "flag.add", "%flag%", flag);
                    }
                    case "unusable" -> {
                        data.set(Keys.UNUSABLE.get(), PersistentDataType.BOOLEAN, true);
                        Utils.success(p, "citems", "flag.add", "%flag%", flag);
                    }
                    case "hide" -> {
                        Utils.success(p, "citems", "flag.add", "%flag%", flag) ;
                        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);
                    }
                    case "placeable" -> {
                        if (args.length < 3) {
                            Utils.info(p, "citems", "info.placeable-flag");
                            return;
                        }
                        String parameter = args[2];

                        if (parameter.equals("small")) {
                            data.set(Keys.PLACEABLE.get(), PersistentDataType.INTEGER, 1);
                            Utils.success(p, "citems", "flag.add", "%flag%", flag + "(small)");
                        } else if (parameter.equals("normal")) {
                            data.set(Keys.PLACEABLE.get(), PersistentDataType.INTEGER, 2);
                            Utils.success(p, "citems", "flag.add", "%flag%", flag + "(normal)");
                        } else {
                            Utils.info(p, "citems", "info.placeable-flag");
                            return;
                        }
                    }
                    default -> {
                        sendList(p);
                        return;
                    }
                }
                break;

            case "remove":
                switch (flag) {
                    case "undroppable" -> {
                        data.remove(Keys.UNDROPPABLE.get());
                        Utils.success(p, "citems", "flag.remove", "%flag%", flag);
                    }
                    case "unbreakable" -> {
                        meta.setUnbreakable(false);
                        Utils.success(p, "citems", "flag.remove", "%flag%", flag);
                    }
                    case "unusable" -> {
                        data.remove(Keys.UNUSABLE.get());
                        Utils.success(p, "citems", "flag.remove", "%flag%", flag);
                    }
                    case "hide" -> {
                        meta.removeItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);
                        Utils.success(p, "citems", "flag.remove", "%flag%", flag);
                    }
                    case "placeable" -> {
                        data.remove(Keys.PLACEABLE.get());
                        Utils.success(p, "citems", "flag.remove.placeable");
                    }
                    default -> sendList(p);
                }
                break;
            case "list":
            default:
                sendList(p);
                return;
        }
        item.setItemMeta(meta);
    }

    public void sendList(Player p) {
        Utils.info(p, "citems", "info.usage.flag");
        Utils.noPrefix(p, "citems", "flag.list.undroppable");
        Utils.noPrefix(p, "citems", "flag.list.unbreakable");
        Utils.noPrefix(p, "citems", "flag.list.unusable");
        Utils.noPrefix(p, "citems", "flag.list.hide");
        Utils.noPrefix(p, "citems", "flag.list.placeable");
        Utils.noPrefix(p, "citems", "flag.list.profile_picture");
        Utils.noPrefix(p, "citems", "flag.list.profile_background");
    }
}
