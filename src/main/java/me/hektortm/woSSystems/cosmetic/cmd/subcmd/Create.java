package me.hektortm.woSSystems.cosmetic.cmd.subcmd;

import me.hektortm.woSSystems.cosmetic.CosmeticManager;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Create extends SubCommand {
    private final DAOHub hub;
    public Create(DAOHub hub) {
        this.hub = hub;
    }
    @Override
    public String getName() {
        return "create";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.COSMETIC_CREATE;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        if (args.length < 3) {
            Utils.info(sender, "cosmetics", "info.usage.create");
            return;
        }
        String type = args[0];
        String id = args[1];
        switch (type) {
            case "title":
                if (hub.getTitlesDAO().titleExists(id)) {
                    Utils.error(sender, "cosmetics", "error.title.exists");
                    return;
                }
                String title;
                StringBuilder builder = new StringBuilder();
                for (int i = 2; i < args.length; i++) {
                    if (i == args.length - 1) {
                        builder.append(args[i]);
                    } else {
                        builder.append(args[i] + " ");
                    }

                }
                title = builder.toString();
                hub.getTitlesDAO().createTitle(id, title);
                Utils.success(sender, "cosmetics", "titles.created", "%title%", title, "%id%", id);
                break;
            case "prefix":
                if (hub.getPrefixDAO().prefixExists(id)) {
                    Utils.error(sender, "cosmetics", "error.prefix.exists");
                    return;
                }
                StringBuilder prefixBuilder = new StringBuilder();
                for (int i = 2; i < args.length; i++) {
                    if (i == args.length - 1) {
                        prefixBuilder.append(args[i]);
                    } else {
                        prefixBuilder.append(args[i] + " ");
                    }
                }
                String prefix = prefixBuilder.toString();
                hub.getPrefixDAO().createPrefix(id, prefix);
                Utils.success(sender, "cosmetics", "prefix.created", "%prefix%", prefix, "%id%", id);
                break;
            case "badge":
                if (hub.getBadgeDAO().badgeExists(id)) {
                    Utils.error(sender, "cosmetics", "error.badge.exists");
                    return;
                }
                StringBuilder badgeBuilder = new StringBuilder();
                for (int i = 2; i < args.length; i++) {
                    if (i == args.length - 1) {
                        badgeBuilder.append(args[i]);
                    } else {
                        badgeBuilder.append(args[i] + " ");
                    }
                }
                String badge = badgeBuilder.toString();
                hub.getBadgeDAO().createBadge(id, badge);
                Utils.success(sender, "cosmetics", "badge.created", "%badge%", badge, "%id%", id);
                break;
            default:
                Utils.error(sender, "cosmetics", "error.invalid-type");

        }
    }
}
