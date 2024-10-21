package me.hektortm.woSSystems.systems.citems.commands;


import me.hektortm.woSSystems.systems.citems.commands.subcommands.*;
import me.hektortm.woSSystems.systems.citems.DataManager;
import me.hektortm.woSSystems.systems.interactions.core.InteractionManager;
import me.hektortm.wosCore.LangManager;
import me.hektortm.wosCore.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class CitemCommand implements CommandExecutor {

    private final Map<String, CitemSubCommand> subCommands = new HashMap<>();
    private final DataManager data;
    private final InteractionManager interactionManager;
    private final LangManager lang;
    public File citemsFolder = new File(Bukkit.getServer().getPluginManager().getPlugin("WoSSystems").getDataFolder(), "citems");

    public CitemCommand(DataManager data, InteractionManager interactionManager, LangManager lang) {
        this.data = data;
        this.interactionManager = interactionManager;
        this.lang = lang;

        subCommands.put("save", new SaveCommand(this, data));
        subCommands.put("rename", new NameCommand());
        subCommands.put("update", new UpdateCommand(this, data));
        subCommands.put("lore", new LoreCommand());
        subCommands.put("flag", new FlagCommand(data));
        subCommands.put("delete", new DeleteCommand(this));
        subCommands.put("action", new ActionCommand(interactionManager, lang));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 0) {
            Utils.error(sender, "citems", "error.usage.citem");
            return true;
        }

        String subCommandName = args[0].toLowerCase();
        CitemSubCommand subCommand = subCommands.get(subCommandName);

        if (subCommand != null) {
            subCommand.execute(sender, java.util.Arrays.copyOfRange(args, 1, args.length));
        } else {
            Utils.error(sender, "citems", "error.usage.citem");
        }

        return true;
    }
}
