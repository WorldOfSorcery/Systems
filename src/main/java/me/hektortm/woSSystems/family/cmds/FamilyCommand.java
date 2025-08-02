package me.hektortm.woSSystems.family.cmds;

import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.family.cmds.subcmd.Create;
import me.hektortm.woSSystems.family.cmds.subcmd.Invite;
import me.hektortm.woSSystems.family.cmds.subcmd.Requests;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class FamilyCommand implements CommandExecutor {

    private final Map<String, SubCommand> subCommands = new HashMap<>();
    private final DAOHub hub;


    public FamilyCommand(DAOHub hub) {
        this.hub = hub;
        // Initialize subcommands here
         subCommands.put("create", new Create(hub));
         subCommands.put("invite", new Invite(hub));
         subCommands.put("requests", new Requests(hub));
        // Add more subcommands as needed
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if (args.length == 0) {
            Utils.info(sender, "citems", "info.usage.citem");
            return true;
        }

        String subCommandName = args[0].toLowerCase();
        SubCommand subCommand = subCommands.get(subCommandName);

        if (subCommand != null) {
            if(!(PermissionUtil.hasPermission(sender, subCommand.getPermission()))) return true;
            subCommand.execute(sender, java.util.Arrays.copyOfRange(args, 1, args.length));
        } else {
            Utils.info(sender, "citems", "info.usage.citem");
        }

        return true;
    }




}
