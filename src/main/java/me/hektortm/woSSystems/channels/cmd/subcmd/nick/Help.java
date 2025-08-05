package me.hektortm.woSSystems.channels.cmd.subcmd.nick;

import me.hektortm.woSSystems.channels.cmd.NicknameCommand;
import me.hektortm.woSSystems.utils.HelpUtil;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

public class Help extends SubCommand {

    private final NicknameCommand cmd;

    public Help(NicknameCommand cmd) {
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
        HelpUtil.sendHelp(cmd.permCmds, sender, "nicknames");
    }
}
