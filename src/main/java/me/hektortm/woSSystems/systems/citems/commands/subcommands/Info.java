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

    private final CitemManager citemManager;

    public Info(CitemManager citemManager) {
        this.citemManager = citemManager;
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
        handleInfoCommand(p);
    }


    private boolean handleInfoCommand(Player player) {
        ItemStack item = player.getItemInHand();

        if (item == null || !item.hasItemMeta()) {
            Utils.error(player, "citems", "error.not-citem");
            return true;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            Utils.error(player, "citems", "error.no-meta");
            return true;
        }

        PersistentDataContainer data = meta.getPersistentDataContainer();
        String itemId = data.get(citemManager.getIdKey(), PersistentDataType.STRING);

        if (itemId == null || itemId.isEmpty()) {
            Utils.error(player, "citems", "error.not-citem");
            return true;
        }

        // Display basic Citem information
        player.sendMessage("§aCitem Information:");
        player.sendMessage("§7ID: §f" + itemId);

        // Display custom flags
        boolean undroppable = data.has(citemManager.getUndroppableKey(), PersistentDataType.BYTE) &&
                data.get(citemManager.getUndroppableKey(), PersistentDataType.BYTE) == 1;
        boolean unusable = data.has(citemManager.getUnusableKey(), PersistentDataType.BYTE) &&
                data.get(citemManager.getUnusableKey(), PersistentDataType.BYTE) == 1;

        player.sendMessage("§7Flags:");
        player.sendMessage(" §7- §eUndroppable: §f" + (undroppable ? "Yes" : "No"));
        player.sendMessage(" §7- §eUnusable: §f" + (unusable ? "Yes" : "No"));

        // Display actions
        String leftAction = data.get(citemManager.getLeftActionKey(), PersistentDataType.STRING);
        String rightAction = data.get(citemManager.getRightActionKey(), PersistentDataType.STRING);

        player.sendMessage("§7Actions:");
        player.sendMessage(" §7- §eLeft-Click Action: §f" + (leftAction != null ? leftAction : "None"));
        player.sendMessage(" §7- §eRight-Click Action: §f" + (rightAction != null ? rightAction : "None"));

        return true;
    }
}
