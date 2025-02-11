package me.hektortm.woSSystems.systems.citems.commands.subcommands;


import me.hektortm.woSSystems.systems.citems.commands.CitemCommand;
import me.hektortm.woSSystems.systems.citems.CitemManager;
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

import java.io.File;

public class SaveCommand extends SubCommand {

    private final CitemCommand cmd;
    private final CitemManager data;

    public SaveCommand(CitemCommand cmd, CitemManager data) {
        this.cmd = cmd;
        this.data = data;
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

        if (args.length != 1) {
            Utils.info(p, "citems", "info.usage.save");
            return;
        }

        if(!data.getErrorHandler().handleCitemErrors(itemInHand, p)) return;

        String id = args[0];
        ItemMeta meta = itemInHand.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(data.getIdKey(), PersistentDataType.STRING, id);
        itemInHand.setItemMeta(meta);
        data.getCitemDAO().saveCitem(id, itemInHand);
        Utils.successMsg1Value(p, "citems", "saved", "%id%", id);
    }
}
