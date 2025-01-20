package me.hektortm.woSSystems.listeners;

import me.hektortm.woSSystems.Database.DatabaseManager;
import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.economy.commands.Coinflip;
import me.hektortm.woSSystems.systems.unlockables.UnlockableManager;
import me.hektortm.woSSystems.time.TimeManager;
import me.hektortm.wosCore.WoSCore;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

public class QuitListener implements Listener {

    private final UnlockableManager manager;
    private final DatabaseManager database;
    private final WoSCore core;
    private final Coinflip coinflip;
    private final WoSSystems plugin;

    public QuitListener(WoSCore core, UnlockableManager manager, DatabaseManager database, Coinflip coinflip, WoSSystems plugin) {
        this.core = core;
        this.manager = manager;
        this.database = database;
        this.coinflip = coinflip;
        this.plugin = plugin;
    }


    @EventHandler
    public void leaveEvent(PlayerQuitEvent event) {
        Player p = event.getPlayer();

        event.setQuitMessage(null);

        if (coinflip.challengeQueue.containsKey(p.getUniqueId())) {
            coinflip.challengeQueue.remove(p.getUniqueId());
        }

        plugin.getBossBarManager().removeBossBar(p);
        try {
            database.removeAllTemps(p.getUniqueId());
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not clear Temp unlockables", e);
        }



    }


}
