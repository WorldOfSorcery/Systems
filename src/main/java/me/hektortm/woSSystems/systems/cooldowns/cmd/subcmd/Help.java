package me.hektortm.woSSystems.systems.cooldowns.cmd.subcmd;

import me.hektortm.woSSystems.systems.cooldowns.cmd.CooldownCommand;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import org.bukkit.command.CommandSender;

public class Help extends SubCommand {

    private final CooldownCommand cmd;

    public Help(CooldownCommand cmd) {
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
        cmd.cooldownHelp(sender);
    }
}
