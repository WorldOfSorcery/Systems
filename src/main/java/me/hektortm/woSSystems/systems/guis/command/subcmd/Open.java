package me.hektortm.woSSystems.systems.guis.command.subcmd;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.systems.guis.GUIManager;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Open extends SubCommand {
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final GUIManager manager = plugin.getGuiManager();
    private final DAOHub hub;

    public Open(DAOHub hub) {
        this.hub = hub;
    }


    @Override
    public String getName() {
        return "open";
    }

    @Override
    public Permissions getPermission() {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {


        Player p = Bukkit.getPlayer(args[0]);
        String id = args[1];

        if(hub.getGuiDAO().getGUIbyId(id) != null) {
            manager.openGUI(p, id);
            sender.sendMessage("Opening GUI '"+id+"' for "+p.getName());
        } else {
            sender.sendMessage("GUI does not exist.");
        }

    }
}
