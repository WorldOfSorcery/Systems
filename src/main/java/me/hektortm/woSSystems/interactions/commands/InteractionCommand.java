package me.hektortm.woSSystems.interactions.commands;


import me.hektortm.woSSystems.interactions.commands.subcommands.*;
import me.hektortm.woSSystems.interactions.core.BindManager;
import me.hektortm.woSSystems.interactions.core.InteractionManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class InteractionCommand implements CommandExecutor {

    private final Map<String, InterSubCommand> subCommands = new HashMap<>();
    private final InteractionManager interManager;
    private final BindManager bindManager;

    public InteractionCommand(InteractionManager interManager, BindManager bindManager) {
        this.interManager = interManager;
        this.bindManager = bindManager;

        subCommands.put("trigger", new TriggerCommand(interManager));
        subCommands.put("help", new HelpCommand(this));
        subCommands.put("reload", new ReloadCommand(interManager));
        subCommands.put("bind", new BindCommand(bindManager));
        subCommands.put("unbind", new UnbindCommand(bindManager));
        subCommands.put("view", new ViewCommand(interManager));

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommandName = args[0].toLowerCase();
        InterSubCommand subCommand = subCommands.get(subCommandName);

        if (subCommand != null) {
            subCommand.execute(sender, java.util.Arrays.copyOfRange(args, 1, args.length));
        } else {
            sender.sendMessage("Unknown subcommand: " + subCommandName);
        }

        return true;
    }

    public void sendHelp(CommandSender sender) {
        if(sender.hasPermission("interactions.help"))
        if(sender instanceof Player p) {
            String cmd = "/interaction";

            p.sendMessage("§7Interaction help:");
            p.sendMessage("§e"+cmd+" reload [id] §7- reload all or a specific interaction");
            p.sendMessage("§e"+cmd+" trigger <player> <id> §7- Trigger an interaction for a player");
            p.sendMessage("§e"+cmd+" view <id> §7- View the actions of an interaction");
            p.sendMessage("§e"+cmd+" bind <id> §7- Bind an interaction to a block");
            p.sendMessage("§e"+cmd+" unbind <id> §7- Unbind an interaction");
            p.sendMessage("§e"+cmd+" npcbind <id> §7- Bind an interaction to an npc");
            p.sendMessage("§e"+cmd+" help §7- Display this help message");
        } else {
            sender.sendMessage("You are not a player...");
        }
    }

}
