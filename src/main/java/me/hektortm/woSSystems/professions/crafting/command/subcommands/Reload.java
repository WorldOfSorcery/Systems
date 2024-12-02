package me.hektortm.woSSystems.professions.crafting.command.subcommands;

import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import org.bukkit.command.CommandSender;

public class Reload extends SubCommand {
    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.CRECIPE_RELOAD;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

    }
}
