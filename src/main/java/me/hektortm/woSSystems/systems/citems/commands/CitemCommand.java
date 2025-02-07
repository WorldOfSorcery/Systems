package me.hektortm.woSSystems.systems.citems.commands;


import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.systems.citems.commands.subcommands.*;
import me.hektortm.woSSystems.systems.citems.CitemManager;
import me.hektortm.woSSystems.systems.interactions.InteractionManager;
import me.hektortm.woSSystems.systems.stats.StatsManager;
import me.hektortm.woSSystems.utils.PermissionUtil;
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
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final CitemManager data = plugin.getCitemManager();
    private final StatsManager statsManager = plugin.getStatsManager();
    private final InteractionManager interactionManager;
    private final LangManager lang = plugin.getLangManager();
    private final LogManager log = plugin.getLogManager();

    public CitemCommand(InteractionManager interactionManager) {

        this.interactionManager = interactionManager;
        subCommands.put("save", new SaveCommand(this, data));
        subCommands.put("rename", new NameCommand(data));
        subCommands.put("update", new UpdateCommand(this, data));
        subCommands.put("lore", new LoreCommand(data));
        subCommands.put("flag", new FlagCommand(data));
        subCommands.put("delete", new DeleteCommand(this, log, data));
        subCommands.put("action", new ActionCommand(data, interactionManager));
        subCommands.put("tag", new Tag(data));
        subCommands.put("info", new Info(data));
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
            if(!(PermissionUtil.hasPermission(sender, subCommand.getPermission()))) return true;
            subCommand.execute(sender, java.util.Arrays.copyOfRange(args, 1, args.length));
        } else {
            Utils.error(sender, "citems", "error.usage.citem");
        }

        return true;
    }


}
