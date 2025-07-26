package me.hektortm.woSSystems.family.cmds;

import me.hektortm.woSSystems.utils.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class FamilyCommand implements CommandExecutor {

    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public FamilyCommand() {
        // Initialize subcommands here
        // subCommands.put("create", new CreateFamilyCommand());
        // subCommands.put("invite", new InviteFamilyCommand());
        // subCommands.put("leave", new LeaveFamilyCommand());
        // Add more subcommands as needed
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        return false;
    }




}
