package me.hektortm.woSSystems.systems.citems.commands.subcommands;


import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.Keys;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class Save extends SubCommand {

    private final DAOHub hub;

    public Save(DAOHub hub) {
        this.hub = hub;
    }

    @Override
    public String getName() {
        return "save";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.CITEM_SAVE;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!PermissionUtil.isPlayer(sender)) return;

        Player p = (Player) sender;

        ItemStack itemInHand = p.getInventory().getItemInMainHand();

        if (args.length < 1) {
            Utils.info(p, "citems", "info.usage.save");
            return;
        }

        if (itemInHand.getType() == Material.AIR) {
            Utils.error(p, "citems", "error.holding-item");
            return;
        }

        String id = args[0];
        ItemMeta meta = itemInHand.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(Keys.ID.get(), PersistentDataType.STRING, id);
        itemInHand.setItemMeta(meta);
        hub.getCitemDAO().saveCitem(id, itemInHand);
        Utils.successMsg1Value(p, "citems", "saved", "%id%", id);
    }
}
