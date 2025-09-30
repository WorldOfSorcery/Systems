package me.hektortm.woSSystems.systems.dialogs.cmd.subcmd;

import me.hektortm.woSSystems.systems.dialogs.cmd.DialogCommand;
import me.hektortm.woSSystems.utils.HelpUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import org.bukkit.command.CommandSender;

public class Help extends SubCommand {
    private final DialogCommand cmd;

    public Help(DialogCommand cmd) {
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
        HelpUtil.sendHelp(cmd.permCmds, sender, "dialogs");
    }
}
