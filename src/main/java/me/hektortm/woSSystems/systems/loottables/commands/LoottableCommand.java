package me.hektortm.woSSystems.systems.loottables.commands;


import me.hektortm.woSSystems.systems.loottables.commands.subcommands.Give;
import me.hektortm.woSSystems.utils.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class LoottableCommand implements CommandExecutor {

    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public LoottableCommand() {
        subCommands.put("give", new Give());
        subCommands.put("reload", new Reload());

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        return true;
    }
}
