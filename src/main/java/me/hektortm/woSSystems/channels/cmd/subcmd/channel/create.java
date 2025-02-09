package me.hektortm.woSSystems.channels.cmd.subcmd.channel;

import me.hektortm.woSSystems.channels.ChannelManager;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
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
            Utils.info(player, "channel", "info.usage.create.usage");
            Utils.noPrefix(player, "channel", "info.usage.create.options");
            Utils.noPrefix(player, "channel", "info.usage.create.forcejoin");
            Utils.noPrefix(player, "channel", "info.usage.create.autojoin");
            Utils.noPrefix(player, "channel", "info.usage.create.hidden");
            Utils.noPrefix(player, "channel", "info.usage.create.default");
            Utils.noPrefix(player, "channel", "info.usage.create.broadcast");
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
                    Utils.error(player, "channel", "error.unknown-option");
                    return;
            }
        }

        // Create the channel
        channelManager.createChannel("§7", name, shortName, "{player}: {message}", new ArrayList<>(), defaultChannel, autoJoin, forceJoin, hiddenFromList, null, broadcastable, -1);
        Utils.success(player, "channel", "created.success", "%channelName%", name);
        Utils.noPrefix(player, "channel", "created.settings");
        Utils.noPrefix(player, "channel", "created.forcejoin", "%status%", (forceJoin ? "§aEnabled" : "§cDisabled"));
        Utils.noPrefix(player, "channel", "created.autojoin", "%status%", (autoJoin ? "§aEnabled" : "§cDisabled"));
        Utils.noPrefix(player, "channel", "created.hidden", "%status%", (hiddenFromList ? "§aEnabled" : "§cDisabled"));
        Utils.noPrefix(player, "channel", "created.default", "%status%", (defaultChannel ? "§aEnabled" : "§cDisabled"));
        Utils.noPrefix(player, "channel", "created.broadcast", "%status%", (broadcastable ? "§aEnabled" : "§cDisabled"));
        channelManager.saveChannels();
    }
}