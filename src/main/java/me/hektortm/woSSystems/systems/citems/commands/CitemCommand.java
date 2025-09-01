package me.hektortm.woSSystems.systems.citems.commands;

import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.systems.citems.commands.subcommands.*;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class CitemCommand implements CommandExecutor {

    private final Map<String, SubCommand> subCommands = new HashMap<>();
    public Map<Permissions, String> permCmds = new HashMap<>();
    private final DAOHub hub;


    public CitemCommand(DAOHub hub) {
        this.hub = hub;

        subCommands.put("save", new Save(hub));
        subCommands.put("update", new Update(hub));
        subCommands.put("rename", new Name());
        subCommands.put("lore", new Lore());
        subCommands.put("flag", new Flag());
        subCommands.put("action", new Action());
        subCommands.put("tooltip", new Tooltip());
        subCommands.put("model", new Model());
        subCommands.put("color", new Color());
        subCommands.put("tag", new Tag());
        subCommands.put("info", new Info());



        for (SubCommand subCommand : subCommands.values()) {
            permCmds.put(subCommand.getPermission(), subCommand.getName());
        }

        subCommands.put("help", new Help(this));

    }

    // /citem -> rename <-

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
            Utils.info(sender, "citems", "help.header");

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
