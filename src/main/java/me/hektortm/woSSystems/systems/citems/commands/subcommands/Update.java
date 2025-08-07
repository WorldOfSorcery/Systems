package me.hektortm.woSSystems.systems.citems.commands.subcommands;


import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Update extends SubCommand {

    private final DAOHub hub;

    public Update(DAOHub hub) {
        this.hub = hub;
    }

    @Override
    public String getName() {
        return "update";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.CITEM_UPDATE;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!PermissionUtil.isPlayer(sender)) return;

        Player p = (Player) sender;
        ItemStack itemInHand = p.getInventory().getItemInMainHand();

        if (args.length < 1) {
            Utils.info(p, "citems", "info.usage.update");
            return;
        }

        if (itemInHand.getType() == Material.AIR) {
            Utils.error(p, "citems", "error.holding-item");
            return;
        }

        String id = args[0];

        if (!hub.getCitemDAO().citemExists(id)) {
            Utils.error(p, "citems", "error.not-found");
            return;
        }

        hub.getCitemDAO().updateCitem(id, itemInHand);
        Utils.successMsg(p, "citems", "updated");
    }
}
