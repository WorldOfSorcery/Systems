package me.hektortm.woSSystems.systems.loottables.commands.subcommands;

import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Give extends SubCommand {
    @Override
    public String getName() {
        return "give";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.LOOTTABLE_GIVE;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player p = Bukkit.getPlayer(args[0]);
        String id = args[1].toLowerCase();



    }
}
