package me.hektortm.woSSystems.player;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.hektortm.woSSystems.content.Json;
import me.hektortm.woSSystems.utils.types.CosmeticType;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Everything the plugin keeps in memory for one player, loaded from wos-api in a
 * single call ({@code GET /v1/players/{uuid}/session}) when the player logs in.
 *
 * <p>The DAOs read and mutate these collections directly (they are concurrent)
 * and mirror every change to the API through {@link ApiWriter}. The session is
 * the source of truth while the player is online.</p>
 */
public final class PlayerSession {

    /** A cosmetic the player owns. */
    public record OwnedCosmetic(String id, CosmeticType type, String obtainedAt) {}

    /** Profile card cosmetics; any field may be null. */
    public record Profile(String background, String backgroundId, String picture, String pictureId) {
        public static final Profile EMPTY = new Profile(null, null, null, null);
    }

    private final UUID uuid;
    private final String username;

    public final Map<String, Long> balances = new ConcurrentHashMap<>();
    public final Map<String, Long> stats = new ConcurrentHashMap<>();
    public final Set<String> permanentUnlocks = ConcurrentHashMap.newKeySet();
    public final Set<String> tempUnlocks = ConcurrentHashMap.newKeySet();
    /** Owned cosmetics keyed by {@link #cosmeticKey}. */
    public final Map<String, OwnedCosmetic> cosmetics = new ConcurrentHashMap<>();
    public final Map<CosmeticType, String> equippedCosmetics = new ConcurrentHashMap<>();
    /** Global cooldown id → start. */
    public final Map<String, Instant> cooldowns = new ConcurrentHashMap<>();
    /** {@link #localCooldownKey} → start. */
    public final Map<String, Instant> localCooldowns = new ConcurrentHashMap<>();
    /** Quest id → the quest engine's saved state document. */
    public final Map<String, JsonObject> quests = new ConcurrentHashMap<>();
    public final Set<String> completedQuests = ConcurrentHashMap.newKeySet();

    private volatile String nickname;
    private volatile Profile profile = Profile.EMPTY;

    public PlayerSession(UUID uuid, String username) {
        this.uuid = uuid;
        this.username = username;
    }

    public UUID uuid() { return uuid; }

    public String username() { return username; }

    @Nullable
    public String nickname() { return nickname; }

    public void setNickname(@Nullable String nickname) { this.nickname = nickname; }

    public Profile profile() { return profile; }

    public void setProfile(Profile profile) { this.profile = profile; }

    public static String cosmeticKey(CosmeticType type, String id) {
        return type.name() + ":" + id;
    }

    public static String localCooldownKey(String cooldownId, String interactionKey) {
        return cooldownId + "\u0000" + interactionKey;
    }

    /** Builds a session from the API's session document. */
    public static PlayerSession fromJson(JsonObject json) {
        JsonObject player = Json.object(json, "player");
        PlayerSession s = new PlayerSession(UUID.fromString(Json.str(player, "uuid")), Json.str(player, "username"));

        for (JsonObject b : objects(json, "balances")) s.balances.put(Json.str(b, "currency"), Json.lng(b, "amount", 0));
        for (JsonObject st : objects(json, "stats")) s.stats.put(Json.str(st, "stat_id"), Json.lng(st, "value", 0));
        for (JsonObject u : objects(json, "unlockables")) {
            (Json.bool(u, "temp", false) ? s.tempUnlocks : s.permanentUnlocks).add(Json.str(u, "unlockable_id"));
        }
        for (JsonObject c : objects(json, "cosmetics")) {
            CosmeticType type;
            try {
                type = CosmeticType.valueOf(Json.str(c, "cosmetic_type").toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                continue; // a type this plugin version does not know
            }
            String id = Json.str(c, "cosmetic_id");
            s.cosmetics.put(cosmeticKey(type, id), new OwnedCosmetic(id, type, Json.str(c, "obtained_at")));
            if (Json.bool(c, "equipped", false)) s.equippedCosmetics.put(type, id);
        }
        s.nickname = Json.str(json, "nickname");
        for (JsonObject c : objects(json, "cooldowns")) {
            s.cooldowns.put(Json.str(c, "cooldown_id"), Instant.parse(Json.str(c, "started_at")));
        }
        for (JsonObject c : objects(json, "local_cooldowns")) {
            s.localCooldowns.put(localCooldownKey(Json.str(c, "cooldown_id"), Json.str(c, "interaction_key")),
                    Instant.parse(Json.str(c, "started_at")));
        }
        JsonObject p = Json.object(json, "profile");
        s.profile = new Profile(Json.str(p, "background"), Json.str(p, "background_id"), Json.str(p, "picture"), Json.str(p, "picture_id"));
        for (JsonObject q : objects(json, "quests")) s.quests.put(Json.str(q, "quest_id"), q);
        for (JsonElement q : Json.array(json, "completed_quests")) s.completedQuests.add(q.getAsString());
        return s;
    }

    private static Iterable<JsonObject> objects(JsonObject json, String field) {
        return () -> Json.array(json, field).asList().stream().map(JsonElement::getAsJsonObject).iterator();
    }
}
