package me.hektortm.woSSystems.professions.commands;

import org.bukkit.command.CommandSender;

public abstract class SubCommand {

    public abstract String getName();

    public abstract void execute(CommandSender sender, String[] args);


}
