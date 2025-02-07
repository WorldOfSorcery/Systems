package me.hektortm.woSSystems.tablist;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.utils.Icons;
import net.kyori.adventure.audience.Audiences;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class TablistManager {
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);

    public void runTablist() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                setTablist(player);
            }
        }, 0L, 100L);
    }

    private void setTablist(Player player) {
        Component header = Component.text("\n \n \n"+ "§f"+Icons.BANNER.getIcon());
        Component footer = Component.text("Online Players: " + Bukkit.getOnlinePlayers().size());

        player.sendPlayerListHeaderAndFooter(header, footer);
    }
}
