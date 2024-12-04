package me.hektortm.woSSystems.professions.crafting.command.subcommands;

import me.hektortm.woSSystems.professions.crafting.CRecipeManager;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import org.bukkit.command.CommandSender;

public class Reload extends SubCommand {

    private final CRecipeManager manager;

    public Reload(CRecipeManager manager) {
        this.manager = manager;
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.CRECIPE_RELOAD;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        manager.loadRecipes();
    }
}
