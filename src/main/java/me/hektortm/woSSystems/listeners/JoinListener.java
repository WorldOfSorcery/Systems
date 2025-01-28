package me.hektortm.woSSystems.listeners;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.time.TimeManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    private final WoSSystems plugin;

    public JoinListener(WoSSystems plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPlayedBefore()) {
            event.setJoinMessage("§7(New) §x§7§a§9§9§7§2§o"+player.getName()+" joined");
            plugin.getChannelManager().autoJoin(player);
        } else {
            event.setJoinMessage("§x§7§a§9§9§7§2§o"+player.getName()+" joined");
        }

        plugin.getBossBarManager().createBossBar(player);
        plugin.getRegionBossBarManager().createBossBar(player);

    }


}
