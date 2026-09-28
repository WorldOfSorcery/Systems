package me.hektortm.woSSystems.player;

import me.hektortm.woSSystems.content.ContentRegistry;
import me.hektortm.wosCore.api.WosApi;

import java.util.logging.Logger;

/**
 * What a DAO needs to work against wos-api, passed as one value:
 * the client (blocking reads), the content registry (definitions), the player
 * sessions (online state) and the writer (background persistence).
 */
public record ApiServices(WosApi api, ContentRegistry content, PlayerSessions sessions, ApiWriter writer, Logger log) {}
