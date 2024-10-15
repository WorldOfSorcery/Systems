package me.hektortm.woSSystems.unlockables.commands;

import me.hektortm.woSSystems.unlockables.UnlockableManager;
import me.hektortm.woSSystems.unlockables.commands.subcommands.*;
import me.hektortm.wosCore.Utils;
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
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 0) {
            Utils.error(sender, "stats", "error.usage.stats"); //TODO
            return true;
        }

        String subCommandName = args[0].toLowerCase();
        UnlockableSubCommand subCommand = subCommands.get(subCommandName);

        if (subCommand != null) {
            subCommand.execute(sender, java.util.Arrays.copyOfRange(args, 1, args.length));
        } else {
            Utils.error(sender, "stats", "error.usage.stats"); //TODO
        }


        return true;
    }
}
