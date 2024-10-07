package me.hektortm.woSSystems.citems.commands;

import org.bukkit.command.CommandSender;

public abstract class CitemSubCommand {

    public abstract String getName();

    public abstract void execute(CommandSender sender, String[] args);

}
