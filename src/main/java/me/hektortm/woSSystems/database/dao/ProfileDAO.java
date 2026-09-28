package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.player.ApiServices;
import me.hektortm.woSSystems.player.ApiWriter;
import me.hektortm.woSSystems.player.PlayerSession;
import me.hektortm.woSSystems.player.PlayerSession.Profile;
import me.hektortm.woSSystems.player.PlayerSessions;

import java.util.UUID;

import static me.hektortm.woSSystems.player.ApiWriter.body;

/**
 * Profile card customisation (background and profile picture), kept in the
 * player's {@link PlayerSession}; changes are partial {@code PUT profile} writes.
 */
public class ProfileDAO {
    private final PlayerSessions sessions;
    private final ApiWriter writer;

    public ProfileDAO(ApiServices s) {
        this.sessions = s.sessions();
        this.writer = s.writer();
    }

    public String getBackground(UUID uuid) { return profile(uuid).background(); }

    public String getBackgroundID(UUID uuid) { return profile(uuid).backgroundId(); }

    public String getProfilePicture(UUID uuid) { return profile(uuid).picture(); }

    public String getProfilePictureID(UUID uuid) { return profile(uuid).pictureId(); }

    /** Sets the background (its item id is the legacy placeholder {@code "e"}). */
    public void updateBackground(UUID uuid, String background) {
        PlayerSession s = sessions.get(uuid);
        if (s != null) {
            Profile p = s.profile();
            s.setProfile(new Profile(background, "e", p.picture(), p.pictureId()));
        }
        writer.put(path(uuid), body("background", background, "background_id", "e"));
    }

    public void updateProfilePicture(UUID uuid, String pictureItem, String id) {
        PlayerSession s = sessions.get(uuid);
        if (s != null) {
            Profile p = s.profile();
            s.setProfile(new Profile(p.background(), p.backgroundId(), pictureItem, id));
        }
        writer.put(path(uuid), body("picture", pictureItem, "picture_id", id));
    }

    private Profile profile(UUID uuid) {
        PlayerSession s = sessions.getOrFetch(uuid);
        return s == null ? PlayerSession.Profile.EMPTY : s.profile();
    }

    private static String path(UUID uuid) {
        return "/v1/players/" + uuid + "/profile";
    }
}
