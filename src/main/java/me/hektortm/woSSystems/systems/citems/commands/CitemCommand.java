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
        subCommands.put("equippable", new Equippable());



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
}
