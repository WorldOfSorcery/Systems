package me.hektortm.woSSystems.family.cmds.subcmd;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.family.Family;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Invite extends SubCommand {
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final DAOHub hub;

    public Invite(DAOHub hub) {
        this.hub = hub;
    }

    @Override
    public String getName() {
        return "invite";
    }

    @Override
    public Permissions getPermission() {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("Usage: /family invite <player_name>");
            return;
        }

        Player p = (Player) sender;
        OfflinePlayer playerName = Bukkit.getOfflinePlayer(args[0]);
        Player targetPlayer = Bukkit.getPlayer(playerName.getUniqueId());

        Family family = hub.getFamilyDAO().getPlayersActiveFamily(p.getUniqueId());

        hub.getFamilyDAO().invitePlayerToFamily(family.getId(), playerName.getUniqueId());
        sender.sendMessage("Invitation sent to " + playerName + "!");
        targetPlayer.sendMessage("You have been invited to join the family: " + family.getName() + "! Use /family accept <family> to join.");

    }
}
