package me.hektortm.woSSystems.cosmetic.cmd;

import me.hektortm.woSSystems.cosmetic.CosmeticManager;
import me.hektortm.woSSystems.cosmetic.cmd.subcmd.Create;
import me.hektortm.woSSystems.cosmetic.cmd.subcmd.Give;
import me.hektortm.woSSystems.cosmetic.cmd.subcmd.Description;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class CosmeticCommand implements CommandExecutor {
    private final Map<String, SubCommand> subCommands = new HashMap<>();
    private final CosmeticManager manager;
    private final DAOHub hub;

    public CosmeticCommand(CosmeticManager manager, DAOHub hub) {
        this.manager = manager;
        this.hub = hub;

        subCommands.put("create", new Create(hub));
        subCommands.put("give", new Give(hub));
        subCommands.put("desc", new Description(hub));


    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You must be a player to use this command!");
        }
        assert sender instanceof Player;
        Player p = (Player) sender;

        if (args.length == 0) {
            manager.openMainPage(p);
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
