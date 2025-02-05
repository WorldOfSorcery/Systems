package me.hektortm.woSSystems.cosmetic.cmd.subcmd;

import me.hektortm.woSSystems.cosmetic.CosmeticManager;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.command.CommandSender;

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
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        String type = args[0];
        String id = args[1];
        switch (type) {
            case "title":
                String title;
                StringBuilder builder = new StringBuilder();
                for (int i = 2; i < args.length; i++) {
                    builder.append(args[i] + " ");
                }
                title = builder.toString();
                hub.getTitlesDAO().createTitle(id, title);
                sender.sendMessage("Created title '"+title+"' with id '"+id+"'");
                break;
            case "prefix":
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
                sender.sendMessage("Created Prefix '"+prefix+"' with id '"+id+"'");
                break;
            case "badge":
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
                sender.sendMessage("Created Badge '"+badge+"' with id '"+id+"'");
                break;
            default:
                sender.sendMessage("Use: title, prefix or badge");

        }
    }
}
