package me.hektortm.woSSystems.time.cmd;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.time.TimeManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Calender implements CommandExecutor {

    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final TimeManager manager = plugin.getTimeManager();


    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        // TODO
        Player p = (Player) commandSender;

        p.openInventory(manager.createCalenderInventory());

        return true;
    }
}
