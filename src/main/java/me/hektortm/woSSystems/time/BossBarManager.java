package me.hektortm.woSSystems.time;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BossBarManager {

    private final Map<UUID, BossBar> playerBars = new HashMap<>();

    public void createBossBar(Player p) {
        BossBar bossBar = Bukkit.createBossBar("", BarColor.WHITE, BarStyle.SOLID);
        bossBar.setVisible(true);
        bossBar.addPlayer(p);
        playerBars.put(p.getUniqueId(), bossBar);
    }

    public void updateBossBar(Player p, String time, String date, String activityName) {
        BossBar bossBar = playerBars.get(p.getUniqueId());

        if (bossBar == null) {
            // If the player doesn't have a BossBar, we don't proceed
            return;
        }

        // If there is an active activity, use its name as the title; otherwise, use the time and date
        String title = (activityName != null) ? String.format("%s | %s | %s", time, date, activityName) : String.format("%s | %s", time, date);
        bossBar.setTitle(title);
    }


    public void removeBossBar(Player p) {
        BossBar bossBar = playerBars.remove(p.getUniqueId());
        if (bossBar != null) {
            bossBar.removePlayer(p);
        }
    }
}
