package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.content.ApiSource;
import me.hektortm.woSSystems.content.ContentRegistry;
import me.hektortm.woSSystems.content.ContentStore;
import me.hektortm.woSSystems.content.Json;
import me.hektortm.woSSystems.utils.model.Constant;
import me.hektortm.wosCore.api.WosApi;

import java.util.logging.Logger;

/** Server-wide key/value {@link Constant}s, served from wos-api ({@code /v1/content/constants}). */
public class ConstantDAO {
    private final ContentStore<Constant> store;

    public ConstantDAO(ContentRegistry registry, WosApi api, Logger log) {
        this.store = registry.register(new ContentStore<>("constants", "Constant",
                ApiSource.flat(api, "/v1/content/constants", "id",
                        j -> new Constant(Json.str(j, "id"), Json.str(j, "value")), log)));
    }

    /** The constant, or {@code null} if no such constant exists. */
    public Constant getConstant(String id) {
        return store.get(id);
    }
}
