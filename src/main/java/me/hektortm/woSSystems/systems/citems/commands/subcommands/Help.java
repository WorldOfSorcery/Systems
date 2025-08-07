package me.hektortm.woSSystems.systems.citems.commands.subcommands;

import me.hektortm.woSSystems.systems.citems.commands.CitemCommand;
import me.hektortm.woSSystems.utils.HelpUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import org.bukkit.command.CommandSender;

public class Help extends SubCommand {

    private final CitemCommand cmd;

    public Help(CitemCommand cmd) {
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
        HelpUtil.sendHelp(cmd.permCmds, sender, "citems");
    }
}
