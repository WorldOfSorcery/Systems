package me.hektortm.woSSystems.economy.commands;

import me.hektortm.woSSystems.economy.commands.subcommands.*;
import me.hektortm.woSSystems.economy.EcoManager;
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

public class EcoCommand implements CommandExecutor {

    private final Map<String, SubCommand> subCommands = new HashMap<>();
    private final EcoManager manager;
    private final LangManager lang;


    public EcoCommand(EcoManager manager, LangManager lang) {
        this.manager = manager;
        this.lang = lang;

        subCommands.put("give", new GiveCommand(manager, lang));
        subCommands.put("take", new TakeCommand(manager, lang));
        subCommands.put("random", new RandomCommand(manager, lang));
        subCommands.put("set", new SetCommand(manager));
        subCommands.put("reset", new ResetCommand(manager));
        subCommands.put("currencies", new CurrenciesCommand(manager));
        subCommands.put("help", new HelpCommand(this));

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 0) {
            ecoHelp(sender);
            return true;
        }

        String subCommandName = args[0].toLowerCase();
        SubCommand subCommand = subCommands.get(subCommandName);

        if (subCommand != null) {
            subCommand.execute(sender, java.util.Arrays.copyOfRange(args, 1, args.length));
        } else {
            ecoHelp(sender);
        }

        return true;
    }

    public void ecoHelp(CommandSender s) {
        if(PermissionUtil.hasAnyPermission(s, Permissions.ECONOMY_SET,
                Permissions.ECONOMY_RESET, Permissions.ECONOMY_GIVE, Permissions.ECONOMY_CURRENCIES, Permissions.ECONOMY_RANDOM,
                Permissions.ECONOMY_TAKE, Permissions.ECONOMY_PAY, Permissions.BALANCE_SELF, Permissions.BALANCE_OTHERS )) {
            if(PermissionUtil.hasPermissionNoMsg(s, Permissions.ECONOMY_CURRENCIES)) s.sendMessage(lang.getMessage("economy", "help.currencies"));
            if(PermissionUtil.hasPermissionNoMsg(s, Permissions.ECONOMY_GIVE)) s.sendMessage(lang.getMessage("economy", "help.give"));
            if(PermissionUtil.hasPermissionNoMsg(s, Permissions.ECONOMY_TAKE)) s.sendMessage(lang.getMessage("economy", "help.take"));
            if(PermissionUtil.hasPermissionNoMsg(s, Permissions.ECONOMY_SET)) s.sendMessage(lang.getMessage("economy", "help.set"));
            if(PermissionUtil.hasPermissionNoMsg(s, Permissions.ECONOMY_RANDOM)) s.sendMessage(lang.getMessage("economy", "help.random"));
            if(PermissionUtil.hasPermissionNoMsg(s, Permissions.ECONOMY_RESET)) s.sendMessage(lang.getMessage("economy", "help.reset"));
            if(PermissionUtil.hasAnyPermission(s, Permissions.BALANCE_OTHERS, Permissions.BALANCE_SELF)) {
                if(!PermissionUtil.hasPermissionNoMsg(s, Permissions.BALANCE_OTHERS)) {
                    s.sendMessage(lang.getMessage("economy", "help.balance"));
                } else {
                    s.sendMessage(lang.getMessage("economy", "help.balance.others"));
                }
            }
            if(PermissionUtil.hasPermissionNoMsg(s, Permissions.ECONOMY_PAY)) s.sendMessage(lang.getMessage("economy", "help.pay"));
            s.sendMessage(lang.getMessage("economy", "help.list"));


        } else {
            Utils.error(s, "general", "error.perms");
        }
    }

}
