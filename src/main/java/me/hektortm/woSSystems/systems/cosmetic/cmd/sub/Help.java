package me.hektortm.woSSystems.systems.cosmetic.cmd.sub;

import me.hektortm.woSSystems.systems.cosmetic.cmd.CosmeticCommand;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import org.bukkit.command.CommandSender;

public class Help extends SubCommand {
    private final CosmeticCommand cmd;

    public Help(CosmeticCommand cmd) {
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
        cmd.cosmeticHelp(sender);
    }
}
