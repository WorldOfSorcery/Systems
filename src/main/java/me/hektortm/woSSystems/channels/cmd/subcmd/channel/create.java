package me.hektortm.woSSystems.channels.cmd.subcmd.channel;

import me.hektortm.woSSystems.channels.ChannelManager;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class create extends SubCommand {
    private final ChannelManager channelManager;

    public create(ChannelManager channelManager) {
        this.channelManager = channelManager;
    }

    @Override
    public String getName() {
        return "create";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.CHANNEL_CREATE;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!PermissionUtil.isPlayer(sender)) return;

        Player player = (Player) sender;

        // Check for minimum required arguments
        if (args.length < 3) {
            player.sendMessage("§cUsage: /ch create <name> <shortName> [-f] [-a]");
            player.sendMessage("§7Options:");
            player.sendMessage("§7- §f-f§7: Force join (players are automatically added to the channel)");
            player.sendMessage("§7- §f-a§7: Auto join (players join the channel automatically when they log in)");
            player.sendMessage("§7- §f-h§7: Hidden (hides the channel from the channel list)");
            return;
        }

        String name = args[1];
        String shortName = args[2];

        // Parse optional flags
        boolean forceJoin = false;
        boolean autoJoin = false;
        boolean hiddenFromList = false;

        for (int i = 3; i < args.length; i++) {
            switch (args[i].toLowerCase()) {
                case "-f":
                    forceJoin = true;
                    break;
                case "-a":
                    autoJoin = true;
                    break;
                case "-h":
                    hiddenFromList = true;
                    break;
                default:
                    player.sendMessage("§cUnknown option: " + args[i]);
                    player.sendMessage("§7Valid options: §f-f§7, §f-a");
                    return;
            }
        }

        // Create the channel
        channelManager.createChannel(name, shortName, "{player}: {message}", new ArrayList<>(), autoJoin, forceJoin, hiddenFromList, -1);
        player.sendMessage("§aChannel §f" + name + " §acreated successfully.");
        player.sendMessage("§7Settings:");
        player.sendMessage("§7- Force Join: " + (forceJoin ? "§aEnabled" : "§cDisabled"));
        player.sendMessage("§7- Auto Join: " + (autoJoin ? "§aEnabled" : "§cDisabled"));
        player.sendMessage("§7- Hidden: " + (hiddenFromList ? "§aEnabled" : "§cDisabled"));
        channelManager.saveChannels();
    }
}