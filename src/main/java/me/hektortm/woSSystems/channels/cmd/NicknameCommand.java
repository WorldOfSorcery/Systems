package me.hektortm.woSSystems.channels.cmd;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.channels.cmd.subcmd.nick.Request;
import me.hektortm.woSSystems.channels.cmd.subcmd.nick.Requests;
import me.hektortm.woSSystems.channels.cmd.subcmd.nick.Reserve;
import me.hektortm.woSSystems.channels.cmd.subcmd.nick.Unreserve;
import me.hektortm.woSSystems.channels.NicknameManager;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class NicknameCommand implements CommandExecutor {

    private final Map<String, SubCommand> subCommands = new HashMap<>();
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final NicknameManager manager = plugin.getNickManager();

    public NicknameCommand() {

        subCommands.put("request", new Request());
        subCommands.put("requests", new Requests());
        subCommands.put("reserve", new Reserve());
        subCommands.put("unreserve", new Unreserve());

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 0) {
            return true;
        }

        String subCommandName = args[0].toLowerCase();
        SubCommand subCommand = subCommands.get(subCommandName);

        if (subCommand != null) {
            if(PermissionUtil.hasPermission(sender, subCommand.getPermission())) {
                subCommand.execute(sender, java.util.Arrays.copyOfRange(args, 1, args.length));
            } else {
                return true;
            }
        } else {
            sender.sendMessage("Unknown subcommand: " + subCommandName);
        }

        return true;
    }
}
