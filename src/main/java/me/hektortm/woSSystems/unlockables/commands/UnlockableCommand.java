package me.hektortm.woSSystems.unlockables.commands;

import me.hektortm.woSSystems.unlockables.UnlockableManager;
import me.hektortm.woSSystems.unlockables.commands.subcommands.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class UnlockableCommand implements CommandExecutor {

    private final Map<String, UnlockableSubCommand> subCommands = new HashMap<>();
    private final UnlockableManager manager;

    public UnlockableCommand(UnlockableManager manager) {

        this.manager = manager;

        subCommands.put("create", new CreateCommand(manager));
        subCommands.put("give", new GiveCommand(manager));
        subCommands.put("take", new TakeCommand(manager));
        subCommands.put("delete", new DeleteCommand(manager));

    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return false;
    }
}
