package me.hektortm.woSSystems.time.cmd.subcmd;

import me.hektortm.woSSystems.time.TimeManager;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import org.bukkit.command.CommandSender;

public class ReloadCommand extends SubCommand {

    private final TimeManager manager;

    public ReloadCommand(TimeManager manager) {
        this.manager = manager;
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public Permissions getPermission() {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("time.reload")) {
            sender.sendMessage("You don't have permission to use this command!");
            return;
        }
        sender.sendMessage("reloading time-config.yml...");
        manager.reloadConfig();
        sender.sendMessage("time-config.yml reloaded!");


    }
}
