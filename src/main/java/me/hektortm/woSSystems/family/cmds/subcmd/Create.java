package me.hektortm.woSSystems.family.cmds.subcmd;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Create extends SubCommand {
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
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
        if (args.length < 1) {
            sender.sendMessage("Usage: /family create <family_name>");
            return;
        }
        Player player = (Player) sender;
        StringBuilder sb = new StringBuilder();
        for (String arg : args) {
            sb.append(arg).append(" ");
        }
        String familyName = sb.toString().trim();
        hub.getFamilyDAO().createFamily(player.getUniqueId(), familyName);
        sender.sendMessage("Family '" + familyName + "' created successfully!");
    }
}
