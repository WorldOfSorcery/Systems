package me.hektortm.woSSystems.systems.citems.commands;


import me.hektortm.woSSystems.systems.citems.commands.subcommands.*;
import me.hektortm.woSSystems.systems.citems.CitemManager;
import me.hektortm.woSSystems.systems.interactions.core.InteractionManager;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.LangManager;
import me.hektortm.wosCore.Utils;
import me.hektortm.wosCore.logging.LogManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class CitemCommand implements CommandExecutor {

    private final Map<String, SubCommand> subCommands = new HashMap<>();
    private final CitemManager data;
    private final InteractionManager interactionManager;
    private final LangManager lang;
    private final LogManager log;
    public File citemsFolder = new File(Bukkit.getServer().getPluginManager().getPlugin("WoSSystems").getDataFolder(), "citems");

    public CitemCommand(CitemManager data, InteractionManager interactionManager, LangManager lang, LogManager log) {
        this.data = data;
        this.interactionManager = interactionManager;
        this.lang = lang;
        this.log = log;

        subCommands.put("save", new SaveCommand(this, data));
        subCommands.put("rename", new NameCommand());
        subCommands.put("update", new UpdateCommand(this, data));
        subCommands.put("lore", new LoreCommand());
        subCommands.put("flag", new FlagCommand(data));
        subCommands.put("delete", new DeleteCommand(this, log));
        subCommands.put("action", new ActionCommand(interactionManager, lang));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 0) {
            Utils.error(sender, "citems", "error.usage.citem");
            return true;
        }

        String subCommandName = args[0].toLowerCase();
        SubCommand subCommand = subCommands.get(subCommandName);

        if (subCommand != null) {
            subCommand.execute(sender, java.util.Arrays.copyOfRange(args, 1, args.length));
        } else {
            Utils.error(sender, "citems", "error.usage.citem");
        }

        return true;
    }


}
