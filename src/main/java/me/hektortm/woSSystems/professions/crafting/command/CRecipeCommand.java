package me.hektortm.woSSystems.professions.crafting.command;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.professions.crafting.CRecipeManager;
import me.hektortm.woSSystems.professions.crafting.command.subcommands.Create;
import me.hektortm.woSSystems.professions.crafting.command.subcommands.Reload;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CRecipeCommand implements CommandExecutor {

    private final WoSSystems plugin;
    private final CRecipeManager manager;
    private final File recipesFolder;
    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public CRecipeCommand(WoSSystems plugin, CRecipeManager manager) {
        this.plugin = plugin;
        this.manager = manager;
        this.recipesFolder = new File(plugin.getDataFolder(), "CRecipes");
        if (!recipesFolder.exists()) {
            recipesFolder.mkdirs();
        }

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

        if(!PermissionUtil.hasPermission(sender, subCommand.getPermission())) return true;

        if (subCommand != null) {
            subCommand.execute(sender, java.util.Arrays.copyOfRange(args, 1, args.length));
        } else {
            crecipeHelp(sender);
        }
        return true;
    }

    public void crecipeHelp(CommandSender sender) {

        return;
    }

}
