package me.hektortm.woSSystems.core;

import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.systems.economy.cmd.Coinflip;
import me.hektortm.woSSystems.systems.unlockables.UnlockableManager;
import me.hektortm.wosCore.WoSCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

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
        plugin.getInteractionManager().getHologramManager().removeAllHolograms(p);
        hub.evictPlayerData(p.getUniqueId());
        plugin.getPlayerRegions().remove(p.getUniqueId());




    }


}
