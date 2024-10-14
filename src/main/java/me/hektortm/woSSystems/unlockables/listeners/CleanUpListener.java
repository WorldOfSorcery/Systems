package me.hektortm.woSSystems.unlockables.listeners;

import me.hektortm.woSSystems.unlockables.UnlockableManager;
import me.hektortm.wosCore.WoSCore;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class CleanUpListener implements Listener {

    private final UnlockableManager manager;
    private final WoSCore core;
    public CleanUpListener(WoSCore core, UnlockableManager manager) {
        this.core = core;
        this.manager = manager;
    }


    @EventHandler
    public void leaveEvent(PlayerQuitEvent event) {
        Player p = event.getPlayer();

        File playerFile = new File(core.getDataFolder() + "playerdata" + File.separator + p.getUniqueId()+".yml");
        FileConfiguration playerData = YamlConfiguration.loadConfiguration(playerFile);

        List<String> tempUnlockables = playerData.getStringList("tempunlockables");
        tempUnlockables.clear();
        try {
            playerData.save(playerFile);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Could not save playerfile: "+ p.getUniqueId() + ".yml");
        }
    }

}
