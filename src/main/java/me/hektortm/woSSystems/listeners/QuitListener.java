package me.hektortm.woSSystems.listeners;

import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.economy.commands.Coinflip;
import me.hektortm.woSSystems.systems.unlockables.UnlockableManager;
import me.hektortm.wosCore.WoSCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.SQLException;
import java.util.logging.Level;

public class QuitListener implements Listener {

    private final UnlockableManager manager;
    private final DAOHub hub;
    private final WoSCore core;
    private final Coinflip coinflip;
    private final WoSSystems plugin;

    public QuitListener(WoSCore core, UnlockableManager manager, DAOHub hub, Coinflip coinflip, WoSSystems plugin) {
        this.core = core;
        this.manager = manager;
        this.hub = hub;
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
        hub.getUnlockableDAO().removeAllTemps(p.getUniqueId());




    }


}
