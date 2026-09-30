package me.hektortm.woSSystems.systems.guis;

import com.destroystokyo.paper.profile.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Player-head profiles by player name. An online player's profile (with their
 * skin) is used right away. Anyone else is looked up once at Mojang, off the
 * main thread, and cached; until that lookup is done (and for names that don't
 * exist) the head gets a name-only profile, and the next draw shows the skin.
 */
final class HeadProfiles {

    private static final long TTL_MS = 6 * 60 * 60 * 1000L; // skins rarely change

    private record Entry(PlayerProfile profile, long at) {}

    private final Plugin plugin;
    private final Map<String, Entry> cache = new ConcurrentHashMap<>();
    private final Set<String> looking = ConcurrentHashMap.newKeySet();

    HeadProfiles(Plugin plugin) {
        this.plugin = plugin;
    }

    PlayerProfile profile(String name) {
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) return online.getPlayerProfile();

        String key = name.toLowerCase(Locale.ROOT);
        Entry cached = cache.get(key);
        if (cached != null && System.currentTimeMillis() - cached.at() < TTL_MS) return cached.profile();

        if (looking.add(key)) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    PlayerProfile found = Bukkit.createProfile(name);
                    found.complete(true); // blocking Mojang lookup: never on the main thread
                    cache.put(key, new Entry(found, System.currentTimeMillis()));
                } finally {
                    looking.remove(key);
                }
            });
        }
        return cached != null ? cached.profile() : Bukkit.createProfile(name);
    }
}
