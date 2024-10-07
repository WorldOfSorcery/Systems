package me.hektortm.woSSystems.stats.commands;

import org.bukkit.command.CommandSender;

public abstract class StatsSubCommand {

    public abstract String getName();

    public abstract void execute(CommandSender sender, String[] args);

}
