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
        if (args.length < 2) {
            player.sendMessage("§cUsage: /ch create <name> <shortName> [-f] [-a]");
            player.sendMessage("§7Options:");
            player.sendMessage("§7- §f-f§7: Force join (players are automatically added to the channel)");
            player.sendMessage("§7- §f-a§7: Auto join (players join the channel automatically when they log in)");
            player.sendMessage("§7- §f-h§7: Hidden (hides the channel from the channel list)");
            player.sendMessage("§7- §f-d§7: Default Channel (when joining this channel gets focused)");
            player.sendMessage("§7- §f-b§7: Broadcastable (adds the ability to broadcast in this channel)");
            return;
        }

        String name = args[0];
        String shortName = args[1];

        // Parse optional flags
        boolean forceJoin = false;
        boolean autoJoin = false;
        boolean hiddenFromList = false;
        boolean defaultChannel = false;
        boolean broadcastable = false;

        for (int i = 2; i < args.length; i++) {
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
                case "-d":
                    defaultChannel = true;
                    break;
                case "-b":
                    broadcastable = true;
                    break;
                default:
                    player.sendMessage("§cUnknown option: " + args[i]);
                    player.sendMessage("§7Valid options: §f-f§7, §f-a");
                    return;
            }
        }

        // Create the channel
        channelManager.createChannel(name, shortName, "{player}: {message}", new ArrayList<>(), defaultChannel, autoJoin, forceJoin, hiddenFromList, null, broadcastable, -1);
        player.sendMessage("§aChannel §f" + name + " §acreated successfully.");
        player.sendMessage("§7Settings:");
        player.sendMessage("§7- Force Join: " + (forceJoin ? "§aEnabled" : "§cDisabled"));
        player.sendMessage("§7- Auto Join: " + (autoJoin ? "§aEnabled" : "§cDisabled"));
        player.sendMessage("§7- Hidden: " + (hiddenFromList ? "§aEnabled" : "§cDisabled"));
        player.sendMessage("§7- Default Channel: "+ (defaultChannel ? "§aEnabled" : "§cDisabled"));
        player.sendMessage("§7- Broadcastable: " + (broadcastable ? "§aEnabled" : "§cDisabled"));
        channelManager.saveChannels();
    }
}