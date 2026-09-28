package me.hektortm.woSSystems.player;

import com.google.gson.JsonElement;
import me.hektortm.wosCore.api.ApiException;
import me.hektortm.wosCore.api.WosApi;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Owns every online player's {@link PlayerSession}.
 *
 * <ul>
 *   <li><b>Login</b> ({@link AsyncPlayerPreLoginEvent}, off the main thread): the
 *       player row is upserted and the full session loaded from wos-api. If that
 *       fails the login is refused — never let a player in with empty data that
 *       could then be written over their real data.</li>
 *   <li><b>Quit</b>: the session is dropped one tick later, after every other
 *       quit handler has run.</li>
 *   <li><b>Offline players</b>: {@link #getOrFetch} loads a read-only snapshot
 *       (blocking, briefly cached) for commands that target offline players.</li>
 * </ul>
 * DAOs that keep a cross-player index subscribe with {@link #onLoad}/{@link #onUnload}.
 */
public final class PlayerSessions implements Listener {
    private static final long OFFLINE_TTL_MS = 30_000;
    static final String KICK_MESSAGE = "§cYour player data could not be loaded right now.\n§7Please try again in a moment.";

    private record Snapshot(PlayerSession session, long fetchedAt) {}

    private final Plugin plugin;
    private final WosApi api;
    private final Map<UUID, PlayerSession> online = new ConcurrentHashMap<>();
    private final Map<UUID, Snapshot> offline = new ConcurrentHashMap<>();
    private final List<Consumer<PlayerSession>> loadHooks = new CopyOnWriteArrayList<>();
    private final List<Consumer<PlayerSession>> unloadHooks = new CopyOnWriteArrayList<>();

    public PlayerSessions(Plugin plugin, WosApi api) {
        this.plugin = plugin;
        this.api = api;
    }

    /** Runs after a player's session is loaded (async thread). */
    public void onLoad(Consumer<PlayerSession> hook) { loadHooks.add(hook); }

    /** Runs when a player's session is dropped (main thread). */
    public void onUnload(Consumer<PlayerSession> hook) { unloadHooks.add(hook); }

    /** The online player's session, or {@code null} if not online/loaded. */
    @Nullable
    public PlayerSession get(UUID uuid) {
        return online.get(uuid);
    }

    /**
     * The online session, else a recent read-only snapshot, else a fresh blocking
     * load; {@code null} if the player is unknown or the API is unavailable.
     * Mutating a snapshot changes nothing persistent — writes go through the DAOs.
     */
    @Nullable
    public PlayerSession getOrFetch(UUID uuid) {
        PlayerSession s = online.get(uuid);
        if (s != null) return s;
        Snapshot snap = offline.get(uuid);
        if (snap != null && System.currentTimeMillis() - snap.fetchedAt() < OFFLINE_TTL_MS) return snap.session();
        try {
            PlayerSession fetched = fetch(uuid);
            offline.put(uuid, new Snapshot(fetched, System.currentTimeMillis()));
            return fetched;
        } catch (ApiException e) {
            if (!e.isNotFound()) plugin.getLogger().warning("[Sessions] offline lookup of " + uuid + " failed: " + e.getMessage());
            return null;
        }
    }

    private PlayerSession fetch(UUID uuid) throws ApiException {
        JsonElement json = api.getJson("/v1/players/" + uuid + "/session");
        return PlayerSession.fromJson(json.getAsJsonObject());
    }

    // ── lifecycle ─────────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.LOW)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;
        UUID uuid = event.getUniqueId();
        try {
            api.send("PUT", "/v1/players/" + uuid, Map.of("username", event.getName()));
            PlayerSession session = fetch(uuid);
            online.put(uuid, session);
            offline.remove(uuid);
            for (Consumer<PlayerSession> hook : loadHooks) hook.accept(session);
        } catch (ApiException e) {
            plugin.getLogger().severe("[Sessions] could not load " + event.getName() + " (" + uuid + "): " + e.getMessage());
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, KICK_MESSAGE);
        }
    }

    /** Another plugin refused the login after we loaded the session — drop it. */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onLogin(PlayerLoginEvent event) {
        if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) unload(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (Bukkit.getPlayer(uuid) == null) unload(uuid); // not re-joined within the tick
        });
    }

    private void unload(UUID uuid) {
        PlayerSession s = online.remove(uuid);
        if (s != null) for (Consumer<PlayerSession> hook : unloadHooks) hook.accept(s);
    }
}
