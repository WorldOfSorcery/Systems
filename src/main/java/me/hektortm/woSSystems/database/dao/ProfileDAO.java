package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.player.ApiServices;
import me.hektortm.woSSystems.player.ApiWriter;
import me.hektortm.woSSystems.player.PlayerSession;
import me.hektortm.woSSystems.player.PlayerSessions;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import static me.hektortm.woSSystems.player.ApiWriter.body;

/**
 * Profile dialog data — the "about me" bio and the showcased custom items —
 * kept in the player's {@link PlayerSession}.  Bio changes are partial
 * {@code PUT profile} writes; each showcase slot is its own resource.
 */
public class ProfileDAO {
    private final PlayerSessions sessions;
    private final ApiWriter writer;

    public ProfileDAO(ApiServices s) {
        this.sessions = s.sessions();
        this.writer = s.writer();
    }

    /** The bio, or {@code null} if unset or blank. */
    public String getBio(UUID uuid) {
        PlayerSession s = sessions.getOrFetch(uuid);
        String bio = s == null ? null : s.bio();
        return bio == null || bio.isBlank() ? null : bio;
    }

    /** Sets the bio; blank clears it (stored as an empty string). */
    public void setBio(UUID uuid, String bio) {
        String value = bio == null ? "" : bio.strip();
        PlayerSession s = sessions.get(uuid);
        if (s != null) s.setBio(value);
        writer.put(path(uuid), body("bio", value));
    }

    /** Showcased citem ids by slot, in slot order. */
    public SortedMap<Integer, String> getShowcase(UUID uuid) {
        PlayerSession s = sessions.getOrFetch(uuid);
        return s == null ? new TreeMap<>() : new TreeMap<>(s.showcase);
    }

    public void setShowcaseSlot(UUID uuid, int slot, String citemId) {
        PlayerSession s = sessions.get(uuid);
        if (s != null) s.showcase.put(slot, citemId);
        writer.put(showcasePath(uuid, slot), body("citem_id", citemId));
    }

    public void clearShowcaseSlot(UUID uuid, int slot) {
        PlayerSession s = sessions.get(uuid);
        if (s != null) s.showcase.remove(slot);
        writer.delete(showcasePath(uuid, slot));
    }

    private static String path(UUID uuid) {
        return "/v1/players/" + uuid + "/profile";
    }

    private static String showcasePath(UUID uuid, int slot) {
        return "/v1/players/" + uuid + "/showcase/" + slot;
    }
}
