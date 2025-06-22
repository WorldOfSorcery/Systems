package me.hektortm.woSSystems.systems.citems.commands.subcommands;


import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.systems.citems.CitemManager;
import me.hektortm.woSSystems.systems.interactions.InteractionManager;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.LangManager;
import me.hektortm.wosCore.Utils;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

@SuppressWarnings("DuplicatedCode")
public class ActionCommand extends SubCommand {

    private final NamespacedKey leftActionKey;
    private final NamespacedKey rightActionKey;
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final CitemManager citemManager;
    private final InteractionManager manager = plugin.getInteractionManager();
    private final LangManager lang = plugin.getLangManager();


    public ActionCommand(CitemManager citemManager) {
        this.citemManager = citemManager;
        leftActionKey = new NamespacedKey(WoSSystems.getPlugin(WoSSystems.class), "action-left");
        rightActionKey = new NamespacedKey(WoSSystems.getPlugin(WoSSystems.class), "action-right");

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

        if (args.length == 0) {
            Utils.info(p, "citems", "info.usage.action");
            return;
        }

        if (!citemManager.getErrorHandler().handleCitemErrors(itemInHand, p)) return;

        String action = args[0].toLowerCase();
        String actionID = args[1].toLowerCase();

        if (!manager.interactionExist(actionID)) {
            Utils.error(p, "citems", "error.inter-not-found", "%id%", actionID);
            return;
        }

        ItemMeta meta = itemInHand.getItemMeta();


        PersistentDataContainer data = meta.getPersistentDataContainer();

        switch (action) {
            case "left":
                data.set(leftActionKey, PersistentDataType.STRING, actionID);
                Utils.success(p, "citems", "action.set.left", "%action%", actionID);
                break;
            case "right":
                data.set(rightActionKey, PersistentDataType.STRING, actionID);
                Utils.success(p, "citems", "action.set.right", "%action%", actionID);
                break;
            default:
                Utils.error(p, "citems", "error.wrong-action");
                return;
        }
        itemInHand.setItemMeta(meta);
    }
}
