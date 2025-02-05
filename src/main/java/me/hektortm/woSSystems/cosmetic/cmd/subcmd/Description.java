package me.hektortm.woSSystems.cosmetic.cmd.subcmd;

import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
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
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
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
                sender.sendMessage("Set Badge description to: "+ desc);
                break;
            case "prefix":
                hub.getPrefixDAO().setPrefixDescription(id, desc);
                sender.sendMessage("Set Prefix description to: "+ desc);
                break;
            case "title":
                hub.getTitlesDAO().setTitleDescription(id, desc);
                sender.sendMessage("Set Title description to: "+ desc);
                break;
            default:
                sender.sendMessage("Use: Badge, Prefix, Title");
                break;
        }
    }
}
