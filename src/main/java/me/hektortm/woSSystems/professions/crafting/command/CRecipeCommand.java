package me.hektortm.woSSystems.professions.crafting.command;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.professions.crafting.CRecipeManager;
import me.hektortm.woSSystems.professions.crafting.command.subcommands.Create;
import me.hektortm.woSSystems.professions.crafting.command.subcommands.Reload;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class CRecipeCommand implements CommandExecutor {

    private final Map<String, SubCommand> subCommands = new HashMap<>();

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public CRecipeCommand(WoSSystems plugin, CRecipeManager manager) {
        File recipesFolder = new File(plugin.getDataFolder(), "CRecipes");

        if (!recipesFolder.exists()) recipesFolder.mkdirs();


        subCommands.put("create", new Create(manager));
        subCommands.put("reload", new Reload());


    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 0) {
            return false;
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
        sender.sendMessage("USAGE"); //TODO
    }

}
