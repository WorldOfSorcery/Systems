package me.hektortm.woSSystems.systems.citems.commands.subcommands;


import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.systems.interactions.core.InteractionConfig;
import me.hektortm.woSSystems.systems.interactions.core.InteractionManager;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.LangManager;
import me.hektortm.wosCore.Utils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class ActionCommand extends SubCommand {

    private final NamespacedKey leftActionKey;
    private final NamespacedKey rightActionKey;
    private final InteractionManager interactionManager;
    private final LangManager lang;

    public ActionCommand(InteractionManager interactionManager, LangManager lang) {
        leftActionKey = new NamespacedKey(WoSSystems.getPlugin(WoSSystems.class), "action-left");
        rightActionKey = new NamespacedKey(WoSSystems.getPlugin(WoSSystems.class), "action-right");
        this.interactionManager = interactionManager;
        this.lang = lang;
    }

    @Override
    public String getName() {
        return "action";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.CITEM_ACTIONS;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!PermissionUtil.isPlayer(sender)) return;

        Player p = (Player) sender;
        ItemStack itemInHand = p.getInventory().getItemInMainHand();

        if (itemInHand == null || itemInHand.getType() == Material.AIR) {
            Utils.error(p, "citems", "error.holding-item");
            return;
        }

        String action = args[0].toLowerCase();
        String actionID = args[1].toLowerCase();

        InteractionConfig interaction = interactionManager.getInteractionById(actionID);
        if (interaction == null) {
            String message = lang.getMessage("citems", "error.inter-not-found").replace("%id%", actionID);
            sender.sendMessage(lang.getMessage("general", "prefix.error")+message);

            return;
        }

        ItemMeta meta = itemInHand.getItemMeta();

        if (meta == null) return;

        PersistentDataContainer data = meta.getPersistentDataContainer();

        switch (action) {
            case "left":
                data.set(leftActionKey, PersistentDataType.STRING, actionID);
                Utils.successMsg1Value(p, "citems", "action.set.left", "%action%", actionID);
                break;
            case "right":
                data.set(rightActionKey, PersistentDataType.STRING, actionID);
                Utils.successMsg1Value(p, "citems", "action.set.right", "%action%", actionID);
                break;
            default:
                Utils.error(p, "citems", "error.wrong-action");
                return;
        }
        itemInHand.setItemMeta(meta);
    }
}
