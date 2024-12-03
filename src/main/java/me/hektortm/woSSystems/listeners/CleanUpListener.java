package me.hektortm.woSSystems.listeners;

import me.hektortm.woSSystems.economy.commands.Coinflip;
import me.hektortm.woSSystems.systems.unlockables.UnlockableManager;
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
import java.util.List;

public class CleanUpListener implements Listener {

    private final UnlockableManager manager;
    private final WoSCore core;
    private final Coinflip coinflip;

    public CleanUpListener(WoSCore core, UnlockableManager manager, Coinflip coinflip) {
        this.core = core;
        this.manager = manager;
        this.coinflip = coinflip;
    }


    @EventHandler
    public void leaveEvent(PlayerQuitEvent event) {
        Player p = event.getPlayer();

        if (coinflip.challengeQueue.containsKey(p.getUniqueId())) {
            coinflip.challengeQueue.remove(p.getUniqueId());
        }

        File playerFile = new File(core.getDataFolder() + File.separator + "playerdata" + File.separator + p.getUniqueId() + ".yml");
        FileConfiguration playerData = YamlConfiguration.loadConfiguration(playerFile);

        // Get the temp unlockables list and clear it
        List<String> tempUnlockables = playerData.getStringList("tempunlockables");
        tempUnlockables.clear();

        // Set the cleared list back to the player data
        playerData.set("tempunlockables", tempUnlockables);

        try {
            // Save the updated player data file
            playerData.save(playerFile);
            Bukkit.getLogger().info("Cleared temp unlockables for player: " + p.getUniqueId());
        } catch (IOException e) {
            Bukkit.getLogger().severe("Could not save player file: " + p.getUniqueId() + ".yml");
        }
    }


}
