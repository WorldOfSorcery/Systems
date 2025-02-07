package me.hektortm.woSSystems.cosmetic.cmd.subcmd;

import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Give extends SubCommand {
    private final DAOHub hub;

    public Give(DAOHub hub) {
        this.hub = hub;
    }

    @Override
    public String getName() {
        return "give";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.COSMETIC_GIVE;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 3) {
            Utils.info(sender, "cosmetics", "info.usage.give");
            return;
        }
        String type = args[0];
        Player target = Bukkit.getPlayer(args[1]);
        String id = args[2];

        switch (type) {
            case "title":
                if (!hub.getTitlesDAO().titleExists(id)) {
                    Utils.error(sender, "cosmetics", "error.title.exists");
                    return;
                }
                hub.getTitlesDAO().giveTitle(id, target);
                Utils.success(sender, "cosmetics", "titles.given", "%player%", target.getName(), "%title%", hub.getTitlesDAO().getTitleText(id));
                Utils.success(target, "cosmetics", "titles.received", "%title%", hub.getTitlesDAO().getTitleText(id));
                break;

            case "prefix":
                if (!hub.getPrefixDAO().prefixExists(id)) {
                    Utils.error(sender, "cosmetics", "error.prefix.exists");
                    return;
                }
                hub.getPrefixDAO().givePrefix(id, target);
                String prefix = hub.getPrefixDAO().getPrefixText(id);
                Utils.success(sender, "cosmetics", "prefix.given", "%player%", target.getName(), "%prefix%", prefix);
                Utils.success(target, "cosmetics", "prefix.received", "%prefix%", prefix);
                break;

            case "badge":
                if (!hub.getBadgeDAO().badgeExists(id)) {
                    Utils.error(sender, "cosmetics", "error.badge.exists");
                    return;
                }
                hub.getBadgeDAO().giveBadge(id, target);
                String badge = hub.getBadgeDAO().getBadgeText(id);
                Utils.success(sender, "cosmetics", "badge.given", "%player%", target.getName(), "%badge%", badge);
                Utils.success(target, "cosmetics", "badge.received", "%badge%", badge);
                break;
            default:
                Utils.success(sender, "cosmetics", "error.invalid-type");
        }



    }
}
