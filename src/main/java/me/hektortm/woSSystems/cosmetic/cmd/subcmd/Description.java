package me.hektortm.woSSystems.cosmetic.cmd.subcmd;

import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Description extends SubCommand {

    private final DAOHub hub;

    public Description(DAOHub hub) {
        this.hub = hub;
    }


    @Override
    public String getName() {
        return "description";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.COSMETIC_DESC;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player p = (Player) sender;
        if (args.length < 3) {
            Utils.info(p, "cosmetics", "info.usage.description");
            return;
        }
        String type = args[0];
        String id = args[1];
        String desc;
        StringBuilder builder = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            if (i == args.length - 1) builder.append(args[i]);
            else builder.append(args[i] + " ");
        }
        desc = builder.toString();

        switch (type) {
            case "badge":
                hub.getBadgeDAO().setBadgeDescription(id, desc);
                Utils.success(p, "cosmetics", "badge.desc", "%id%", id, "%desc%", desc);
                break;
            case "prefix":
                hub.getPrefixDAO().setPrefixDescription(id, desc);
                Utils.success(p, "cosmetics", "prefix.desc", "%id%", id, "%desc%", desc);
                break;
            case "title":
                hub.getTitlesDAO().setTitleDescription(id, desc);
                Utils.success(p, "cosmetics", "titles.desc", "%id%", id, "%desc%", desc);
                break;
            default:
                Utils.error(p, "cosmetics", "error.invalid-type");
                break;
        }
    }
}
