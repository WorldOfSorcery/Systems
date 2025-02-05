package me.hektortm.woSSystems.cosmetic.cmd.subcmd;

import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
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
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        String type = args[0];
        Player target = Bukkit.getPlayer(args[1]);
        String id = args[2];

        switch (type) {
            case "title":
                if (!hub.getTitlesDAO().titleExists(id)) {
                    sender.sendMessage("This title does not exist.");
                    return;
                }
                hub.getTitlesDAO().giveTitle(id, target);
                sender.sendMessage("Given " + target.getName() + " Title '"+hub.getTitlesDAO().getTitleText(id)+"'");
                break;
            case "prefix":
                if (!hub.getPrefixDAO().prefixExists(id)) {
                    sender.sendMessage("This prefix does not exist.");
                    return;
                }
                hub.getPrefixDAO().givePrefix(id, target);
                sender.sendMessage("Given "+target.getName() + " prefix '"+hub.getPrefixDAO().getPrefixText(id)+"'");
                break;
            case "badge":
                if (!hub.getBadgeDAO().badgeExists(id)) {
                    sender.sendMessage("This badge does not exist.");
                    return;
                }
                hub.getBadgeDAO().giveBadge(id, target);
                sender.sendMessage("Given "+target.getName() + " badge '"+hub.getBadgeDAO().getBadgeText(id)+"'");
                break;
            default:
                sender.sendMessage("This type does not exist.");
        }



    }
}
