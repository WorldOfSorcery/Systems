package me.hektortm.woSSystems.systems.citems.commands.subcommands;

import me.hektortm.woSSystems.systems.citems.CitemManager;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class Info extends SubCommand {

    private final CitemManager manager;

    public Info(CitemManager citemManager) {
        this.manager = citemManager;
    }

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
        Player p = (Player) sender;
        ItemStack item = p.getItemInHand();

        if (manager.getErrorHandler().handleCitemErrors(item, p)) return;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();
        String itemId = data.get(manager.getIdKey(), PersistentDataType.STRING);

        p.sendMessage("§aCitem Information:");
        p.sendMessage("§7ID: §f" + itemId);

        // Display custom flags
        boolean undroppable = data.has(manager.getUndroppableKey(), PersistentDataType.BYTE) &&
                data.get(manager.getUndroppableKey(), PersistentDataType.BYTE) == 1;
        boolean unusable = data.has(manager.getUnusableKey(), PersistentDataType.BYTE) &&
                data.get(manager.getUnusableKey(), PersistentDataType.BYTE) == 1;

        p.sendMessage("§7Flags:");
        p.sendMessage(" §7- §eUndroppable: §f" + (undroppable ? "Yes" : "No"));
        p.sendMessage(" §7- §eUnusable: §f" + (unusable ? "Yes" : "No"));
        boolean hasHideFlags = !meta.getItemFlags().isEmpty();
        p.sendMessage(" §7- §eHide Flags: §f" + (hasHideFlags ? "Yes" : "No"));

        // Display actions
        String leftAction = data.get(manager.getLeftActionKey(), PersistentDataType.STRING);
        String rightAction = data.get(manager.getRightActionKey(), PersistentDataType.STRING);

        p.sendMessage("§7Actions:");
        p.sendMessage(" §7- §eLeft-Click Action: §f" + (leftAction != null ? leftAction : "None"));
        p.sendMessage(" §7- §eRight-Click Action: §f" + (rightAction != null ? rightAction : "None"));
    }


}
