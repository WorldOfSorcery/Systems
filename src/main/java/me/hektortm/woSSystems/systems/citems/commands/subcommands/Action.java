package me.hektortm.woSSystems.systems.citems.commands.subcommands;


import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.systems.interactions.InteractionManager;
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

@SuppressWarnings("DuplicatedCode")
public class Action extends SubCommand {

    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final InteractionManager manager = plugin.getInteractionManager();
    private final DAOHub hub;

    public Action(DAOHub hub) {
        this.hub = hub;
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

        if (itemInHand.getType() == Material.AIR) {
            Utils.error(p, "citems", "error.holding-item");
            return;
        }

        if (args.length == 0) {
            Utils.info(p, "citems", "info.usage.action");
            return;
        }

        String action = args[0].toLowerCase();
        String actionID = args[1].toLowerCase();

        if (!hub.getInteractionDAO().interactionExists(actionID, sender)) return;

        ItemMeta meta = itemInHand.getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();

        switch (action) {
            case "left":
                data.set(Keys.LEFT_ACTION.get(), PersistentDataType.STRING, actionID);
                Utils.success(p, "citems", "action.set.left", "%action%", actionID);
                break;
            case "right":
                data.set(Keys.RIGHT_ACTION.get(), PersistentDataType.STRING, actionID);
                Utils.success(p, "citems", "action.set.right", "%action%", actionID);
                break;
            case "placed":
                data.set(Keys.PLACED_ACTION.get(), PersistentDataType.STRING, actionID);
                Utils.success(p, "citems", "action.set.right", "%action%", actionID);
            default:
                Utils.error(p, "citems", "error.wrong-action");
                return;
        }
        itemInHand.setItemMeta(meta);
    }
}
