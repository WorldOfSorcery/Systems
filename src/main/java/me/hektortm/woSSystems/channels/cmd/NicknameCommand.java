package me.hektortm.woSSystems.channels.cmd;

import me.hektortm.woSSystems.channels.cmd.subcmd.nick.*;
import me.hektortm.woSSystems.channels.NicknameManager;
import me.hektortm.woSSystems.utils.HelpUtil;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class NicknameCommand implements CommandExecutor {

    private final Map<String, SubCommand> subCommands = new HashMap<>();
    public Map<Permissions, String> permCmds = new HashMap<>();

    public NicknameCommand() {

        subCommands.put("requests", new Requests());
        subCommands.put("request", new Request());
        subCommands.put("reserve", new Reserve());
        subCommands.put("unreserve", new Unreserve());

        for (SubCommand subCommand : subCommands.values()) {
            permCmds.put(subCommand.getPermission(), subCommand.getName());
        }
        permCmds.put(Permissions.NICK_RESET, "reset");

        subCommands.put("help", new Help(this));

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 0) {
            HelpUtil.sendHelp(permCmds, sender, "nicknames");
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
