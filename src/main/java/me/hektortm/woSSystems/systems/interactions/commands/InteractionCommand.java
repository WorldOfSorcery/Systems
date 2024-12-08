package me.hektortm.woSSystems.systems.interactions.commands;

import me.hektortm.woSSystems.systems.interactions.commands.subcommands.*;
import me.hektortm.woSSystems.utils.PermissionUtil;
import me.hektortm.woSSystems.utils.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class InteractionCommand implements CommandExecutor {

    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public InteractionCommand() {
        subCommands.put("trigger", new TriggerCommand());
        subCommands.put("help", new HelpCommand(this));
        subCommands.put("reload", new ReloadCommand());
        subCommands.put("bind", new BindCommand());
        subCommands.put("unbind", new UnbindCommand());

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommandName = args[0].toLowerCase();
        SubCommand subCommand = subCommands.get(subCommandName);

        if (subCommand != null) {
            if(PermissionUtil.hasPermission(sender, subCommand.getPermission())) {
                subCommand.execute(sender, java.util.Arrays.copyOfRange(args, 1, args.length));
            } else {
                return true;
            }
        } else {
            sender.sendMessage("Unknown subcommand: " + subCommandName);
        }


        return true;
    }

    // TODO
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
