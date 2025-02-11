package me.hektortm.woSSystems.systems.citems.commands.subcommands;

import me.hektortm.woSSystems.systems.citems.CitemManager;
import me.hektortm.woSSystems.utils.Icons;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.logging.Level;

public class Tag extends SubCommand {
    private final CitemManager manager;

    public Tag(CitemManager manager) {
        this.manager = manager;
    }

    @Override
    public String getName() {
        return "tag";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.CITEM_TAG;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player p)) {
            return;
        }

        if (args.length < 1) {
            Utils.info(p, "citems", "info.usage.tag");
            return;
        }

        ItemStack item = p.getInventory().getItemInMainHand();

        if (manager.getErrorHandler().handleCitemErrors(item, p)) return;

        Icons firstIcon = parseIcon(args[0]);
        Icons secondIcon = args.length > 1 ? parseIcon(args[1]) : null;
        if (firstIcon == null) {
            Utils.error(p, "citems", "error.invalid-icon");
            return;
        }

        addTags(item, firstIcon, secondIcon);
        p.sendMessage("Item tagged successfully!");
        Utils.successMsg(p, "citems", "tag.success");


    }

    private Icons parseIcon(String iconName) {
        try {
            return Icons.valueOf(iconName.toUpperCase());
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().log(Level.INFO, "Illegal Argument");
            return null; // Invalid icon name
        }
    }


    private void addTags(ItemStack item, Icons firstIcon, Icons secondIcon) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.hasLore() ? meta.getLore() : new java.util.ArrayList<>();

        // Combine icons into one lore line
        String iconLine = "§f" + firstIcon.getIcon();
        if (secondIcon != null) {
            iconLine += secondIcon.getIcon();
        }
        lore.add(iconLine);

        // Set updated lore
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

}
