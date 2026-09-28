package me.hektortm.woSSystems.systems.quests.cmd.sub;

import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import org.bukkit.command.CommandSender;

public class Help extends SubCommand {

    @Override
    public String getName() { return "help"; }

    @Override
    public Permissions getPermission() { return null; }

    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage("§6/quest start <id> §7- Start a quest");
        sender.sendMessage("§6/quest start <id> <player> §7- Start a quest for another player");
        sender.sendMessage("§6/quest status [player] §7- View active quest progress");
    }
}
