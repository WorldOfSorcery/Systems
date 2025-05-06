package me.hektortm.woSSystems.systems.citems.commands;


import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.systems.citems.commands.subcommands.*;
import me.hektortm.woSSystems.systems.citems.CitemManager;
import me.hektortm.woSSystems.systems.interactions.InteractionManager;
import me.hektortm.woSSystems.systems.stats.StatsManager;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
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
    private final CitemManager data;
    private final StatsManager statsManager = plugin.getStatsManager();
    private final InteractionManager interactionManager;
    private final LangManager lang = plugin.getLangManager();
    private final LogManager log = plugin.getLogManager();

    public CitemCommand(CitemManager data, InteractionManager interactionManager) {
        this.data = data;

        this.interactionManager = interactionManager;
        subCommands.put("save", new SaveCommand(this, data));
        subCommands.put("rename", new NameCommand(data));
        subCommands.put("update", new UpdateCommand(this, data));
        subCommands.put("lore", new LoreCommand(data));
        subCommands.put("flag", new FlagCommand(data));
        subCommands.put("action", new ActionCommand(data, interactionManager));
        subCommands.put("tag", new Tag(data));
        subCommands.put("info", new Info(data));
        subCommands.put("help", new HelpCommand(this));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 0) {
            Utils.info(sender, "citems", "info.usage.citem");
            return true;
        }

        String subCommandName = args[0].toLowerCase();
        SubCommand subCommand = subCommands.get(subCommandName);

        if (subCommand != null) {
            if(!(PermissionUtil.hasPermission(sender, subCommand.getPermission()))) return true;
            subCommand.execute(sender, java.util.Arrays.copyOfRange(args, 1, args.length));
        } else {
            Utils.info(sender, "citems", "info.usage.citem");
        }

        return true;
    }

    public void citemHelp(CommandSender sender) {
        if (PermissionUtil.hasAnyPermission(sender, Permissions.CITEM_SAVE, Permissions.CITEM_RENAME,
                Permissions.CITEM_UPDATE, Permissions.CITEM_LORE, Permissions.CITEM_FLAGS, Permissions.CITEM_ACTIONS,
                Permissions.CITEM_TAG, Permissions.CITEM_INFO, Permissions.CITEM_GIVE, Permissions.CITEM_REMOVE)) {
            Utils.info(sender, "unlockables", "help.header");

            if (PermissionUtil.hasPermissionNoMsg(sender, Permissions.CITEM_SAVE))
                Utils.noPrefix(sender, "citems", "help.save");

            if(PermissionUtil.hasPermissionNoMsg(sender, Permissions.CITEM_UPDATE))
                Utils.noPrefix(sender, "citems", "help.update");

            if(PermissionUtil.hasPermissionNoMsg(sender, Permissions.CITEM_RENAME))
                Utils.noPrefix(sender, "citems", "help.rename");

            if(PermissionUtil.hasPermissionNoMsg(sender, Permissions.CITEM_LORE))
                Utils.noPrefix(sender, "citems", "help.lore");
            if(PermissionUtil.hasPermissionNoMsg(sender, Permissions.CITEM_FLAGS))
                Utils.noPrefix(sender, "citems", "help.flags");
            if(PermissionUtil.hasPermissionNoMsg(sender, Permissions.CITEM_ACTIONS))
                Utils.noPrefix(sender, "citems", "help.actions");
            if(PermissionUtil.hasPermissionNoMsg(sender, Permissions.CITEM_TAG))
                Utils.noPrefix(sender, "citems", "help.tag");
            if(PermissionUtil.hasPermissionNoMsg(sender, Permissions.CITEM_INFO))
                Utils.noPrefix(sender, "citems", "help.info");
            if(PermissionUtil.hasPermissionNoMsg(sender, Permissions.CITEM_GIVE))
                Utils.noPrefix(sender, "citems", "help.give");
            if(PermissionUtil.hasPermissionNoMsg(sender, Permissions.CITEM_REMOVE))
                Utils.noPrefix(sender, "citems", "help.remove");

            Utils.noPrefix(sender, "citems", "help.help");
        } else {
            Utils.error(sender, "general", "error.perms");
        }
    }

}
