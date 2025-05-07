package me.hektortm.woSSystems.systems.unlockables.commands;

import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.systems.unlockables.UnlockableManager;
import me.hektortm.woSSystems.systems.unlockables.commands.subcommands.*;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.LangManager;
import me.hektortm.wosCore.Utils;
import me.hektortm.wosCore.logging.LogManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class UnlockableCommand implements CommandExecutor {

    private final Map<String, SubCommand> subCommands = new HashMap<>();
    private final DAOHub hub;

    public UnlockableCommand(DAOHub hub) {
        this.hub = hub;

        subCommands.put("give", new GiveCommand(hub));
        subCommands.put("take", new TakeCommand(hub));
        subCommands.put("help", new HelpCommand(this));

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 0) {
            unlockableHelp(sender);
            return true;
        }

        String subCommandName = args[0].toLowerCase();
        SubCommand subCommand = subCommands.get(subCommandName);

        if (subCommand != null) {
            if(!(PermissionUtil.hasPermission(sender, subCommand.getPermission()))) return true;
            subCommand.execute(sender, java.util.Arrays.copyOfRange(args, 1, args.length));
        } else {
            unlockableHelp(sender);
        }
        return true;
    }

    public void unlockableHelp(CommandSender sender) {
        if (PermissionUtil.hasAnyPermission(sender, Permissions.UNLOCKABLE_GIVE, Permissions.UNLOCKABLE_TAKE, Permissions.UNLOCKABLE_DELETE, Permissions.UNLOCKABLE_CREATE)) {
            Utils.info(sender, "unlockables", "help.header");

            if (PermissionUtil.hasPermissionNoMsg(sender, Permissions.UNLOCKABLE_GIVE))
                Utils.noPrefix(sender, "unlockables", "help.give");

            if(PermissionUtil.hasPermissionNoMsg(sender, Permissions.UNLOCKABLE_TAKE))
                Utils.noPrefix(sender, "unlockables", "help.take");

            Utils.noPrefix(sender, "unlockables", "help.help");
        } else {
            Utils.error(sender, "general", "error.perms");
        }
    }

}
