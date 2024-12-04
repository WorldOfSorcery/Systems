package me.hektortm.woSSystems.professions.crafting.command.subcommands;

import me.hektortm.woSSystems.professions.crafting.command.CRecipeCommand;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import org.bukkit.command.CommandSender;

public class Help extends SubCommand {

    private final CRecipeCommand cmd;

    public Help(CRecipeCommand cmd) {
        this.cmd = cmd;
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public Permissions getPermission() {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        cmd.crecipeHelp(sender);
    }
}
