package me.hektortm.woSSystems.systems.unlockables.commands;

import org.bukkit.command.CommandSender;

public abstract class UnlockableSubCommand {

    public abstract String getName();

    public abstract void execute(CommandSender sender, String[] args);

}
