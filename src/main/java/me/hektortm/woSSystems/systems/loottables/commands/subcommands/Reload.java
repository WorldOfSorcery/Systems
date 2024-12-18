package me.hektortm.woSSystems.systems.loottables.commands.subcommands;

import me.hektortm.woSSystems.systems.loottables.LoottableManager;
import me.hektortm.woSSystems.utils.Permissions;
import me.hektortm.woSSystems.utils.SubCommand;
import me.hektortm.wosCore.Utils;
import org.bukkit.command.CommandSender;

public class Reload extends SubCommand {
    private final LoottableManager manager;
    public Reload(LoottableManager manager) {
        this.manager = manager;
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public Permissions getPermission() {
        return Permissions.LOOTTABLE_RELOAD;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        manager.loadLoottables();
        Utils.successMsg(sender, "loottables", "reload");
    }
}
