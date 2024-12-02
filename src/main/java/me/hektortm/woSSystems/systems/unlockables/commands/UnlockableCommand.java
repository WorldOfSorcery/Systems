package me.hektortm.woSSystems.systems.unlockables.commands;

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
    private final UnlockableManager manager;
    private final LangManager lang;
    private final LogManager logManager;

    public UnlockableCommand(UnlockableManager manager, LangManager lang, LogManager logManager) {

        this.manager = manager;
        this.lang = lang;
        this.logManager = logManager;

        subCommands.put("create", new CreateCommand(manager));
        subCommands.put("delete", new DeleteCommand(manager, logManager));
        subCommands.put("give", new GiveCommand(manager));
        subCommands.put("take", new TakeCommand(manager));
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
            subCommand.execute(sender, java.util.Arrays.copyOfRange(args, 1, args.length));
        } else {
            unlockableHelp(sender);
        }


        return true;
    }

    public void unlockableHelp(CommandSender sender) {
        if (PermissionUtil.hasAnyPermission(sender, Permissions.UNLOCKABLE_GIVE, Permissions.UNLOCKABLE_TAKE, Permissions.UNLOCKABLE_DELETE, Permissions.UNLOCKABLE_CREATE)) {
            Utils.successMsg(sender, "unlockables", "help.perm.header");
            if(PermissionUtil.hasPermissionNoMsg(sender, Permissions.UNLOCKABLE_CREATE)) sender.sendMessage(lang.getMessage("unlockables", "help.perm.create"));
            if(PermissionUtil.hasPermissionNoMsg(sender, Permissions.UNLOCKABLE_DELETE)) sender.sendMessage(lang.getMessage("unlockables", "help.perm.delete"));
            if(PermissionUtil.hasPermissionNoMsg(sender, Permissions.UNLOCKABLE_GIVE)) sender.sendMessage(lang.getMessage("unlockables", "help.perm.give"));
            if(PermissionUtil.hasPermissionNoMsg(sender, Permissions.UNLOCKABLE_TAKE)) sender.sendMessage(lang.getMessage("unlockables", "help.perm.take"));
            sender.sendMessage(lang.getMessage("unlockables", "help.perm.help"));
        } else {
            Utils.error(sender, "general", "error.perms");
        }
    }

}
