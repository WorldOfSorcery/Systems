package me.hektortm.woSSystems.systems.guis.command;


import me.hektortm.woSSystems.systems.guis.command.subcommands.Open;
import me.hektortm.woSSystems.systems.interactions.InteractionManager;
import me.hektortm.woSSystems.systems.guis.GUIManager;
import me.hektortm.woSSystems.utils.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

public class GUIcommand implements CommandExecutor {
    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public GUIcommand(GUIManager manager, InteractionManager interactionManager) {

        subCommands.put("open", new Open(interactionManager, manager));

    }

    //TODO: Rework
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        return false;
    }


}
