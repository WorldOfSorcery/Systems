package me.hektortm.woSSystems.systems.unlockables.commands;

import me.hektortm.woSSystems.systems.unlockables.UnlockableManager;
import me.hektortm.woSSystems.systems.unlockables.commands.subcommands.*;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.wosCore.LangManager;
import me.hektortm.wosCore.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class TempUnlockableCommand implements CommandExecutor {

    private final UnlockableManager manager;
    private final LangManager lang;
    private final Map<String, UnlockableSubCommand> subCommands = new HashMap<>();

    public TempUnlockableCommand(UnlockableManager manager, LangManager lang) {
        this.manager = manager;
        this.lang = lang;


        subCommands.put("create", new TempCreateCommand(manager));
        subCommands.put("delete", new TempDeleteCommand(manager));
        subCommands.put("give", new TempGiveCommand(manager));
        subCommands.put("take", new TempTakeCommand(manager));
        subCommands.put("help", new TempHelpCommand(this));

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 0) {
            tempUnlockableHelp(sender);
            return true;
        }

        String subCommandName = args[0].toLowerCase();
        UnlockableSubCommand subCommand = subCommands.get(subCommandName);

        if (subCommand != null) {
            subCommand.execute(sender, java.util.Arrays.copyOfRange(args, 1, args.length));
        } else {
            tempUnlockableHelp(sender);
        }


        return true;
    }

    public void tempUnlockableHelp(CommandSender sender) {
        if (PermissionUtil.hasAnyPermission(sender, Permissions.UNLOCKABLE_TEMP_GIVE, Permissions.UNLOCKABLE_TEMP_TAKE, Permissions.UNLOCKABLE_TEMP_DELETE, Permissions.UNLOCKABLE_TEMP_CREATE)) {
            Utils.successMsg(sender, "unlockables", "help.temp.header");
            if(PermissionUtil.hasPermissionNoMsg(sender, Permissions.UNLOCKABLE_TEMP_CREATE)) sender.sendMessage(lang.getMessage("unlockables", "help.temp.create"));
            if(PermissionUtil.hasPermissionNoMsg(sender, Permissions.UNLOCKABLE_TEMP_DELETE)) sender.sendMessage(lang.getMessage("unlockables", "help.temp.delete"));
            if(PermissionUtil.hasPermissionNoMsg(sender, Permissions.UNLOCKABLE_TEMP_GIVE)) sender.sendMessage(lang.getMessage("unlockables", "help.temp.give"));
            if(PermissionUtil.hasPermissionNoMsg(sender, Permissions.UNLOCKABLE_TEMP_TAKE)) sender.sendMessage(lang.getMessage("unlockables", "help.temp.take"));
            sender.sendMessage(lang.getMessage("unlockables", "help.temp.help"));
        } else {
            Utils.error(sender, "general", "error.perms");
        }
    }


}
