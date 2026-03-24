package me.hektortm.woSSystems.systems.citems.cmd.sub;

import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import org.bukkit.command.CommandSender;

public class Reload extends SubCommand {
    private final DAOHub daoHub;


    public Reload(DAOHub daoHub) {
        this.daoHub = daoHub;
    }


    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.CITEM_GIVE;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        daoHub.getCitemDAO().preloadAll();
        sender.sendMessage("Citems reloaded");

    }
}
