package me.hektortm.woSSystems.content;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.hektortm.wosCore.api.ApiException;
import me.hektortm.wosCore.api.WosApi;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * {@link ContentStore.Source} backed by a wos-api content resource.
 *
 * <ul>
 *   <li>{@link #flat}: the collection endpoint returns complete entities
 *       ({@code GET /v1/content/constants} → every constant).</li>
 *   <li>{@link #tree}: the collection lists ids only and each entity is
 *       assembled by {@code GET /v1/content/<key>/<id>} (guis, dialogs,
 *       interactions, loottables).</li>
 * </ul>
 *
 * The mapper turns one JSON object into the domain model; an entity whose
 * mapping throws is skipped and logged, so one bad row never blocks the rest.
 */
public final class ApiSource<T> implements ContentStore.Source<T> {

    /** Maps a JSON entity to the model. May throw on malformed data. */
    @FunctionalInterface
    public interface Mapper<T> {
        T map(JsonObject json) throws Exception;
    }

    private final WosApi api;
    private final String collection;
    private final String idField;
    private final boolean tree;
    private final Mapper<T> mapper;
    private final Logger log;

    private ApiSource(WosApi api, String collection, String idField, boolean tree, Mapper<T> mapper, Logger log) {
        this.api = api;
        this.collection = collection;
        this.idField = idField;
        this.tree = tree;
        this.mapper = mapper;
        this.log = log;
    }

    /** Entities come complete from the collection endpoint. */
    public static <T> ApiSource<T> flat(WosApi api, String collection, String idField, Mapper<T> mapper, Logger log) {
        return new ApiSource<>(api, collection, idField, false, mapper, log);
    }

    /** The collection lists ids; each entity is fetched as a tree. */
    public static <T> ApiSource<T> tree(WosApi api, String collection, Mapper<T> mapper, Logger log) {
        return new ApiSource<>(api, collection, "id", true, mapper, log);
    }

    @Override
    public Map<String, T> loadAll() throws ApiException {
        JsonArray rows = api.getJson(collection).getAsJsonArray();
        Map<String, T> out = new LinkedHashMap<>();
        for (JsonElement el : rows) {
            JsonObject row = el.getAsJsonObject();
            String id = row.get(idField).getAsString();
            if (tree) {
                loadOne(id).ifPresent(t -> out.put(id, t));
            } else {
                T mapped = mapSafely(id, row);
                if (mapped != null) out.put(id, mapped);
            }
        }
        return out;
    }

    @Override
    public Optional<T> loadOne(String id) throws ApiException {
        String path = collection + "/" + encode(id);
        try {
            JsonElement json = api.getJson(path);
            if (json == null || !json.isJsonObject()) return Optional.empty();
            return Optional.ofNullable(mapSafely(id, json.getAsJsonObject()));
        } catch (ApiException e) {
            if (e.isNotFound()) return Optional.empty();
            throw e;
        }
    }

    private T mapSafely(String id, JsonObject json) {
        try {
            return mapper.map(json);
        } catch (Exception e) {
            log.warning("[Content] " + collection + ": skipped '" + id + "' — " + e);
            return null;
        }
    }

    private static String encode(String id) {
        return java.net.URLEncoder.encode(id, java.nio.charset.StandardCharsets.UTF_8).replace("+", "%20");
    }

    /** Convenience: map a JSON object into a record/class via Gson (snake_case). */
    public static <R> Function<JsonObject, R> gson(Class<R> type) {
        return json -> WosApi.GSON.fromJson(json, type);
    }
}
