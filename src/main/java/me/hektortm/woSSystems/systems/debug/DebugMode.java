package me.hektortm.woSSystems.systems.debug;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Who has debug mode on ({@code /wosdebug}). While it is on, a staff member
 * sees a label on every bound interaction, GUI texts with their placeholders
 * as written, and in chat what the commands they trigger do. It is off again
 * after a relog.
 */
public final class DebugMode implements Listener {

    private final Set<UUID> on = ConcurrentHashMap.newKeySet();

    public boolean isOn(Player player) {
        return on.contains(player.getUniqueId());
    }

    /** Switches it for the player; true if it is on now. */
    public boolean toggle(Player player) {
        if (on.remove(player.getUniqueId())) return false;
        on.add(player.getUniqueId());
        return true;
    }

    /** Sends a debug line to the player, if they have debug mode on. */
    public void tell(Player player, String line) {
        if (isOn(player)) player.sendMessage(line);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        on.remove(event.getPlayer().getUniqueId());
    }
}
