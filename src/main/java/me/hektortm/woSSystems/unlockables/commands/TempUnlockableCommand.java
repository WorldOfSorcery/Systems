package me.hektortm.woSSystems.unlockables.commands;

import me.hektortm.woSSystems.stats.commands.StatsSubCommand;
import me.hektortm.woSSystems.unlockables.UnlockableManager;
import me.hektortm.woSSystems.unlockables.commands.subcommands.TempCreateCommand;
import me.hektortm.woSSystems.unlockables.commands.subcommands.TempGiveCommand;
import me.hektortm.woSSystems.unlockables.commands.subcommands.TempTakeCommand;
import me.hektortm.wosCore.Utils;
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


        subCommands.put("create", new TempCreateCommand(manager));
        subCommands.put("give", new TempGiveCommand(manager));
        subCommands.put("take", new TempTakeCommand(manager));

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
