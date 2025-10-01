package me.hektortm.woSSystems.systems.interactions.commands;

import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.systems.interactions.commands.subcommands.*;
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

public class InteractionCommand implements CommandExecutor {

    private final Map<String, SubCommand> subCommands = new HashMap<>();
    public Map<Permissions, String> permCmds = new HashMap<>();
    private final DAOHub hub;

    public InteractionCommand(DAOHub hub) {
        this.hub = hub;
        subCommands.put("trigger", new Trigger(hub));
        subCommands.put("bind", new Bind(hub));
        subCommands.put("unbind", new Unbind(hub));
        subCommands.put("npcbind", new NPCBind(hub));
        subCommands.put("npcunbind", new NPCUnbind(hub));
        subCommands.put("info", new Info(hub));

        for(SubCommand subCommand : subCommands.values()) {
            permCmds.put(subCommand.getPermission(), subCommand.getName());
        }

        subCommands.put("help", new Help(this));

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 0) {
            Utils.info(sender, "interactions", "info.usage.interaction");
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
            Utils.info(sender, "interactions", "info.usage.interaction");
        }


        return true;
    }
}
