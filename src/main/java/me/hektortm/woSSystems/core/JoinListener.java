package me.hektortm.woSSystems.core;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    private final WoSSystems plugin;
    private final DAOHub hub;

    public JoinListener(WoSSystems plugin, DAOHub hub) {
        this.plugin = plugin;
        this.hub = hub;
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
        plugin.getChannelManager().forceJoin(player);
        plugin.getChannelManager().joinDefault(player);
        plugin.getBossBarManager().createBossBar(player);
        plugin.getRegionBossBarManager().createBossBar(player);

        // Load player data into memory async so all subsequent reads are instant
        final java.util.UUID uuid = player.getUniqueId();
        org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> hub.loadPlayerData(uuid));
    }


}
