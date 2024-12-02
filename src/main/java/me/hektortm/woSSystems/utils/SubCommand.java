package me.hektortm.woSSystems.utils;

import org.bukkit.command.CommandSender;

public abstract class SubCommand {

    public abstract String getName();

    public abstract Permissions getPermission();

    public abstract void execute(CommandSender sender, String[] args);

}
