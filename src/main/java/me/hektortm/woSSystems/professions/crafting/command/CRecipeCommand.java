package me.hektortm.woSSystems.professions.crafting.command;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.professions.crafting.CRecipeManager;
import me.hektortm.woSSystems.professions.crafting.command.subcommands.Create;
import me.hektortm.woSSystems.professions.crafting.command.subcommands.Help;
import me.hektortm.woSSystems.professions.crafting.command.subcommands.Reload;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.LangManager;
import me.hektortm.wosCore.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class CRecipeCommand implements CommandExecutor {

    private final LangManager lang;

    private final Map<String, SubCommand> subCommands = new HashMap<>();

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public CRecipeCommand(WoSSystems plugin, CRecipeManager manager, LangManager lang) {
        this.lang = lang;
        File recipesFolder = new File(plugin.getDataFolder(), "CRecipes");

        if (!recipesFolder.exists()) recipesFolder.mkdirs();


        subCommands.put("create", new Create(manager));
        subCommands.put("reload", new Reload(manager));
        subCommands.put("help", new Help(this));


    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 0) {
            crecipeHelp(sender);
            return true;
        }

        String subCommandName = args[0].toLowerCase();
        SubCommand subCommand = subCommands.get(subCommandName);

        if (subCommand != null) {
            if(!PermissionUtil.hasPermission(sender, subCommand.getPermission())) return true;
            subCommand.execute(sender, java.util.Arrays.copyOfRange(args, 1, args.length));
        } else {
            crecipeHelp(sender);
        }
        return true;
    }

    public void crecipeHelp(CommandSender sender) {
        if(PermissionUtil.hasAnyPermission(sender, Permissions.CRECIPE_CREATE, Permissions.CRECIPE_CREATE)) {
            Utils.successMsg(sender, "crecipes", "help.header");
            if(PermissionUtil.hasPermissionNoMsg(sender, Permissions.CRECIPE_CREATE)) sender.sendMessage(lang.getMessage("crecipes", "help.create"));
            if(PermissionUtil.hasPermissionNoMsg(sender, Permissions.CRECIPE_RELOAD)) sender.sendMessage(lang.getMessage("crecipes", "help.reload"));
            sender.sendMessage(lang.getMessage("crecipes", "help.list"));
        } else {
            Utils.error(sender, "general", "error.perms");
        }
    }

}
