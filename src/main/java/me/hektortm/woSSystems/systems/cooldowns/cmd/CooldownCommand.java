package me.hektortm.woSSystems.systems.cooldowns.cmd;

import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.systems.cooldowns.cmd.subcmd.Give;
import me.hektortm.woSSystems.systems.cooldowns.cmd.subcmd.Help;
import me.hektortm.woSSystems.systems.cooldowns.cmd.subcmd.Remove;
import me.hektortm.woSSystems.systems.cooldowns.cmd.subcmd.View;
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

public class CooldownCommand implements CommandExecutor {

    private final Map<String, SubCommand> subCommands = new HashMap<>();
    private final DAOHub hub;

    public CooldownCommand(DAOHub hub) {
        this.hub = hub;

        subCommands.put("give", new Give(hub));
        subCommands.put("remove", new Remove(hub));
        subCommands.put("view", new View(hub));
        subCommands.put("help", new Help(this));

    }


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 0) {
            Utils.info(sender, "cooldowns", "info.usage.general");
            return true;
        }

        String subCommandName = args[0].toLowerCase();
        SubCommand subCommand = subCommands.get(subCommandName);

        if (subCommand != null) {
            if(!(PermissionUtil.hasPermission(sender, subCommand.getPermission()))) return true;
            subCommand.execute(sender, java.util.Arrays.copyOfRange(args, 1, args.length));
        } else {
            Utils.info(sender, "cooldowns", "info.usage.general");
        }

        return true;
    }

    public void cooldownHelp(CommandSender sender) {
        if (PermissionUtil.hasAnyPermission(sender, Permissions.COOLDOWNS_GIVE, Permissions.COOLDOWNS_REMOVE,
                Permissions.COOLDOWNS_VIEW)) {
            Utils.info(sender, "cooldowns", "help.header");

            if (PermissionUtil.hasPermissionNoMsg(sender, Permissions.COOLDOWNS_GIVE))
                Utils.noPrefix(sender, "cooldowns", "help.give");

            if(PermissionUtil.hasPermissionNoMsg(sender, Permissions.COOLDOWNS_REMOVE))
                Utils.noPrefix(sender, "cooldowns", "help.remove");

            if(PermissionUtil.hasPermissionNoMsg(sender, Permissions.COOLDOWNS_VIEW))
                Utils.noPrefix(sender, "cooldowns", "help.view");

            Utils.noPrefix(sender, "cooldowns", "help.help");
        } else {
            Utils.error(sender, "general", "error.perms");
        }
    }

}
