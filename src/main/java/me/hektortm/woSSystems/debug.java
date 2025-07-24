package me.hektortm.woSSystems;

import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.systems.interactions.HologramHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class debug implements CommandExecutor {
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final DAOHub hub;
    private final HologramHandler hologramHandler;

    public debug(DAOHub hub) {
        this.hub = hub;
         hologramHandler = new HologramHandler(hub);
    }


    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        Player p = (Player) commandSender;

        p.sendMessage("Cleaning up holograms");

        return true;
    }
}
