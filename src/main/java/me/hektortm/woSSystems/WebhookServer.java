package me.hektortm.woSSystems;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import me.hektortm.woSSystems.database.DAOHub;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.Executors;

public class WebhookServer {

    private final HttpServer server;
    private final DAOHub daoHub;
    private final WoSSystems plugin;
    private final String secret;

    public WebhookServer(WoSSystems plugin, DAOHub daoHub) throws IOException {
        this.plugin = plugin;
        this.daoHub = daoHub;
        this.secret = plugin.getConfig().getString("webhook.secret");
        int port    = plugin.getConfig().getInt("webhook.port", 8080);

        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/invalidate", this::handleInvalidate);
        server.setExecutor(Executors.newFixedThreadPool(2));
    }

    public void start() {
        server.start();
        plugin.getLogger().info("[Webhook] Listening on port " +
                plugin.getConfig().getInt("webhook.port", 8080));
    }

    public void stop() {
        server.stop(0);
    }

    private UUID parseUUID(String raw) {
        if (raw.contains("-")) return UUID.fromString(raw);
        // Insert dashes: 8-4-4-4-12
        String formatted = raw.replaceFirst(
                "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                "$1-$2-$3-$4-$5"
        );
        return UUID.fromString(formatted);
    }

    private void handleInvalidate(HttpExchange exchange) throws IOException {
        // Only POST
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            respond(exchange, 405, "Method Not Allowed");
            return;
        }

        // Validate secret
        String auth = exchange.getRequestHeaders().getFirst("x-webhook-secret");
        if (auth == null || !auth.equals(secret)) {
            respond(exchange, 401, "Unauthorized");
            return;
        }

        // Parse body
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        try {
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
            plugin.getLogger().info("[Webhook] Parsed OK");

            String type = json.get("type").getAsString();
            plugin.getLogger().info("[Webhook] Type: " + type);

            String id = json.get("id").getAsString();
            plugin.getLogger().info("[Webhook] ID: " + id);

            UUID editorUUID = parseUUID(json.get("editor_uuid").getAsString());
            plugin.getLogger().info("[Webhook] UUID: " + editorUUID);

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
                    daoHub.handleWebhookInvalidation(type, id, editorUUID)
            );

            respond(exchange, 200, "{\"status\":\"ok\"}");

        } catch (Exception e) {
            plugin.getLogger().warning("[Webhook] Failed at: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            respond(exchange, 400, "{\"error\":\"invalid payload\"}");
        }
    }

    private void respond(HttpExchange exchange, int code, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}