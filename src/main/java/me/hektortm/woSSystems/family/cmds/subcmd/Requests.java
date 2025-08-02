package me.hektortm.woSSystems.family.cmds.subcmd;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Requests extends SubCommand {
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final DAOHub hub;

    public Requests(DAOHub hub) {
        this.hub = hub;
    }

    @Override
    public String getName() {
        return "requests";
    }

    @Override
    public Permissions getPermission() {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player p = (Player) sender;

        for (String id : hub.getFamilyDAO().getRequests(p.getUniqueId())) {
            p.sendMessage("- " + id);
        }

    }
}
