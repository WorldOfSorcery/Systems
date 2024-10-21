package me.hektortm.woSSystems.systems.interactions.commands;

import org.bukkit.command.CommandSender;

public abstract class InterSubCommand {

    public abstract String getName();

    public abstract void execute(CommandSender sender, String[] args);


}
