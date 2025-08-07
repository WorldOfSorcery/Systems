package me.hektortm.woSSystems.systems.citems.commands.subcommands;

import me.hektortm.woSSystems.utils.Keys;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

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
        Player p = (Player) sender;
        ItemStack item = p.getInventory().getItemInMainHand();

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();
        String itemId = data.get(Keys.ID.get(), PersistentDataType.STRING);

        p.sendMessage("§aCitem Information:");
        p.sendMessage("§7Identifier: §f" + itemId);

        // Display custom flags
        boolean undroppable = data.has(Keys.UNDROPPABLE.get(), PersistentDataType.BOOLEAN);
        boolean unusable = data.has(Keys.UNUSABLE.get(), PersistentDataType.BOOLEAN);

        p.sendMessage("§7Flags:");
        p.sendMessage(" §7- §eUndroppable: §f" + (undroppable ? "Yes" : "No"));
        p.sendMessage(" §7- §eUnusable: §f" + (unusable ? "Yes" : "No"));
        boolean hasHideFlags = !meta.getItemFlags().isEmpty();
        p.sendMessage(" §7- §eHide Flags: §f" + (hasHideFlags ? "Yes" : "No"));

        NamespacedKey modelKey = meta.getItemModel();
        NamespacedKey tooltip = meta.getTooltipStyle();
        boolean hiddenTooltip = meta.isHideTooltip();

        p.sendMessage("§7Model: §f" + (modelKey != null ? modelKey.toString() : "None"));
        p.sendMessage("§7Tooltip Style: §f" + (tooltip != null ? tooltip.toString() : "None"));
        p.sendMessage("§7Hidden Tooltip: §f" + (hiddenTooltip ? "Yes" : "No"));

        // Display actions
        String leftAction = data.get(Keys.LEFT_ACTION.get(), PersistentDataType.STRING);
        String rightAction = data.get(Keys.RIGHT_ACTION.get(), PersistentDataType.STRING);

        p.sendMessage("§7Actions:");
        p.sendMessage(" §7- §eLeft-Click Action: §f" + (leftAction != null ? leftAction : "None"));
        p.sendMessage(" §7- §eRight-Click Action: §f" + (rightAction != null ? rightAction : "None"));
    }


}
