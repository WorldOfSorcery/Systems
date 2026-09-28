package me.hektortm.woSSystems.player;

import me.hektortm.woSSystems.database.AsyncWriteQueue;
import me.hektortm.wosCore.api.ApiException;
import me.hektortm.wosCore.api.WosApi;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Persists game-state changes to wos-api in the background. Writes go through the
 * single-threaded {@link AsyncWriteQueue}, so they reach the API in the order the
 * game made them; a write that fails because the API is briefly unavailable is
 * retried before it is reported.
 */
public final class ApiWriter {
    private static final int ATTEMPTS = 4;

    private final WosApi api;
    private final Logger log;

    public ApiWriter(WosApi api, Logger log) {
        this.api = api;
        this.log = log;
    }

    public void put(String path, Object body) { submit("PUT", path, body, Map.of()); }

    public void post(String path, Object body) { submit("POST", path, body, Map.of()); }

    public void delete(String path) { submit("DELETE", path, null, Map.of()); }

    /**
     * POST that must apply exactly once even when retried (economy transactions):
     * one idempotency key is generated here and reused by every attempt.
     */
    public void postOnce(String path, Object body) {
        submit("POST", path, body, Map.of("Idempotency-Key", java.util.UUID.randomUUID().toString()));
    }

    /** Convenience body: {@code body("value", 5, "temp", true)} → {"value":5,"temp":true}. */
    public static Map<String, Object> body(Object... keyValues) {
        Map<String, Object> m = new java.util.LinkedHashMap<>();
        for (int i = 0; i + 1 < keyValues.length; i += 2) m.put((String) keyValues[i], keyValues[i + 1]);
        return m;
    }

    /** URL-encodes one path segment (ids may contain spaces, commas, colons…). */
    public static String seg(Object value) {
        return URLEncoder.encode(String.valueOf(value), StandardCharsets.UTF_8).replace("+", "%20");
    }

    private void submit(String method, String path, Object body, Map<String, String> headers) {
        AsyncWriteQueue.submit(() -> {
            for (int attempt = 1; ; attempt++) {
                try {
                    api.send(method, path, body, headers);
                    return;
                } catch (ApiException e) {
                    if (!e.isUnavailable() || attempt >= ATTEMPTS) {
                        log.severe("[WosApi] write failed: " + method + " " + path + " — " + e.getMessage());
                        throw e; // AsyncWriteQueue reports it to Discord
                    }
                    Thread.sleep(500L * attempt);
                }
            }
        });
    }
}
