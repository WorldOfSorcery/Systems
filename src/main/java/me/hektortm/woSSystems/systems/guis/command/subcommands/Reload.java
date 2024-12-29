package me.hektortm.woSSystems.systems.guis.command.subcommands;

import me.hektortm.woSSystems.systems.guis.GUIManager;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import org.bukkit.command.CommandSender;

public class Reload extends SubCommand {

    private final GUIManager guiManager;
    public Reload(GUIManager guiManager) {
        this.guiManager = guiManager;
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public Permissions getPermission() {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        guiManager.loadGUIs();
    }
}
