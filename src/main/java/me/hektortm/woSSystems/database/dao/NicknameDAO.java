package me.hektortm.woSSystems.database.dao;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.hektortm.woSSystems.content.Json;
import me.hektortm.woSSystems.player.ApiServices;
import me.hektortm.woSSystems.player.ApiWriter;
import me.hektortm.woSSystems.player.PlayerSession;
import me.hektortm.woSSystems.player.PlayerSessions;
import me.hektortm.wosCore.api.ApiException;
import me.hektortm.wosCore.api.WosApi;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import static me.hektortm.woSSystems.player.ApiWriter.body;

/**
 * Nicknames: each player's active nickname (in their {@link PlayerSession}; the
 * API also keeps the history), pending change requests awaiting staff review,
 * and nicknames reserved for a specific player.
 *
 * <p>Request/reservation lookups are rare staff commands, so they read the API
 * directly (blocking) instead of being cached.</p>
 */
public class NicknameDAO {
    private final WosApi api;
    private final PlayerSessions sessions;
    private final ApiWriter writer;
    private final Logger log;

    public NicknameDAO(ApiServices s) {
        this.api = s.api();
        this.sessions = s.sessions();
        this.writer = s.writer();
        this.log = s.log();
    }

    /** Sets the player's nickname (and appends it to their history). */
    public void saveNickname(UUID uuid, String username, String nickname) {
        PlayerSession s = sessions.get(uuid);
        if (s != null) s.setNickname(nickname);
        writer.put("/v1/players/" + uuid + "/nickname", body("nickname", nickname));
    }

    /** Clears the active nickname; the history is kept. */
    public void resetNickname(UUID uuid) {
        PlayerSession s = sessions.get(uuid);
        if (s != null) s.setNickname(null);
        writer.delete("/v1/players/" + uuid + "/nickname");
    }

    /** The active nickname, or {@code null}. */
    public String getNickname(UUID uuid) {
        PlayerSession s = sessions.getOrFetch(uuid);
        return s == null ? null : s.nickname();
    }

    /**
     * Resolves a username or nickname to the player's display name (nickname if
     * set, else username); {@code null} if no player matches.
     */
    public String getRealNameOrNickname(String input) {
        try {
            JsonObject row = api.getJson("/v1/server/nicknames/lookup?name=" + URLEncoder.encode(input, StandardCharsets.UTF_8))
                    .getAsJsonObject();
            String nickname = Json.str(row, "nickname");
            return nickname != null ? nickname : Json.str(row, "username");
        } catch (ApiException e) {
            if (!e.isNotFound()) log.warning("[Nicknames] lookup of '" + input + "' failed: " + e.getMessage());
            return null;
        }
    }

    // ─── Change requests ────────────────────────────────────────────────────────

    /** Submits (or replaces) a change request; {@code "reset"} requests clearing the nickname. */
    public void requestNicknameChange(UUID uuid, String nickname) {
        writer.put("/v1/server/nick-requests/" + uuid, body("nickname", nickname));
    }

    /** Applies the pending request (or reset) and removes it. */
    public void approveNicknameChange(UUID uuid) {
        String requested = getNickRequests().get(uuid);
        if (requested == null) return;
        if ("reset".equalsIgnoreCase(requested)) {
            resetNickname(uuid);
        } else {
            saveNickname(uuid, null, requested);
        }
        writer.delete("/v1/server/nick-requests/" + uuid);
    }

    public void denyNicknameChange(UUID uuid) {
        writer.delete("/v1/server/nick-requests/" + uuid);
    }

    /** Pending requests, {@code uuid → requested nickname}. */
    public Map<UUID, String> getNickRequests() {
        return uuidMap("/v1/server/nick-requests");
    }

    // ─── Reservations ───────────────────────────────────────────────────────────

    public void reserveNickname(UUID uuid, String nickname) {
        writer.put("/v1/server/reserved-nicks/" + uuid, body("nickname", nickname));
    }

    public void unreserveNickname(UUID uuid) {
        writer.delete("/v1/server/reserved-nicks/" + uuid);
    }

    public boolean isNicknameReserved(String nickname) {
        return getReservedNicknames().values().stream().anyMatch(n -> n.equalsIgnoreCase(nickname));
    }

    /** Reservations, {@code uuid → nickname}. */
    public Map<UUID, String> getReservedNicknames() {
        return uuidMap("/v1/server/reserved-nicks");
    }

    private Map<UUID, String> uuidMap(String path) {
        Map<UUID, String> out = new HashMap<>();
        try {
            for (JsonElement el : api.getJson(path).getAsJsonArray()) {
                JsonObject row = el.getAsJsonObject();
                out.put(UUID.fromString(Json.str(row, "uuid")), Json.str(row, "nickname"));
            }
        } catch (ApiException e) {
            log.warning("[Nicknames] " + path + " failed: " + e.getMessage());
        }
        return out;
    }
}
