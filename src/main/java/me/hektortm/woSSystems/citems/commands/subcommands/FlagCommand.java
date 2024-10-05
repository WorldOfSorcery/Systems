package me.hektortm.woSSystems.citems.commands.subcommands;


import me.hektortm.woSSystems.citems.commands.SubCommand;
import me.hektortm.woSSystems.citems.core.DataManager;
import me.hektortm.wosCore.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class FlagCommand extends SubCommand {

    private final NamespacedKey undroppableKey;
    public FlagCommand(DataManager data) {
        undroppableKey = new NamespacedKey(Bukkit.getPluginManager().getPlugin("WoSSystems"), "undroppable");
    }


    @Override
    public String getName() {
        return "flag";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player p = (Player) sender;

        ItemStack itemInHand = p.getInventory().getItemInMainHand();
        ItemMeta meta = itemInHand.getItemMeta();

        if(!sender.hasPermission("citem.flag")) {
            Utils.error(sender, "general", "error.perms");
        }

        if (args.length < 2) {
            Utils.error(p, "citems", "error.usage.flag");
            return;
        }

        if (itemInHand == null || itemInHand.getType() == Material.AIR) {
            Utils.error(p, "citems", "error.holding-item");
            return;
        }
        if (meta == null) {
            Utils.error(p, "citems", "error.no-meta");
            return;
        }
        String flagCmd = args[0];
        String flag = args[1].toLowerCase();
        switch (flagCmd.toLowerCase()) {
            case "add":
                if (flag.equals("undroppable")) {
                    PersistentDataContainer data = meta.getPersistentDataContainer();
                    data.set(undroppableKey, PersistentDataType.BYTE, (byte) 1);
                    Utils.successMsg(p, "citems", "flag.add.undroppable");
                }
                if (flag.equals("unbreakable")) {
                    meta.setUnbreakable(true);
                    Utils.successMsg(p, "citems", "flag.add.unbreakable");
                }
                break;

            case "remove":
                if (flag.equals("undroppable")) {
                    PersistentDataContainer data = meta.getPersistentDataContainer();
                    data.remove(undroppableKey);
                    Utils.successMsg(p, "citems", "flag.remove.undroppable");
                }
                if (flag.equals("unbreakable")) {
                    meta.setUnbreakable(false);
                    Utils.successMsg(p, "citems", "flag.remove.unbreakable");
                }
                break;

            default:
                Utils.error(p, "citems", "error.usage.flag");
                return;
        }
        itemInHand.setItemMeta(meta);
    }
}
