package me.hektortm.woSSystems.unlockables.commands;

import me.hektortm.woSSystems.unlockables.UnlockableManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class TempUnlockableCommand implements CommandExecutor {

    private final UnlockableManager manager;
    private final Map<String, UnlockableSubCommand> subCommands = new HashMap<>();

    public TempUnlockableCommand(final UnlockableManager manager) {
        this.manager = manager;


    }





    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return false;
    }
}
