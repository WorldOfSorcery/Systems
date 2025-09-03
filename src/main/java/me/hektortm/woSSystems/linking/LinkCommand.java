package me.hektortm.woSSystems.linking;

import me.hektortm.woSSystems.linking.subcmd.Website;
import me.hektortm.woSSystems.utils.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class LinkCommand implements CommandExecutor {

    private final Map<String, SubCommand> subcommands = new HashMap<>();

    public LinkCommand() {
        subcommands.put("website", new Website());
    }


    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {

        if (strings.length == 0) {
            commandSender.sendMessage("Usage: /link <subcommand>");
            return true;
        }

        String subCommandName = strings[0].toLowerCase();
        SubCommand subCommand = subcommands.get(subCommandName);
        if (subCommand != null) {
            subCommand.execute(commandSender, java.util.Arrays.copyOfRange(strings, 1, strings.length));
        } else {
            commandSender.sendMessage("Unknown subcommand: " + subCommandName);
            commandSender.sendMessage("Available subcommands: " + String.join(", ", subcommands.keySet()));
        }



        return true;
    }
}
