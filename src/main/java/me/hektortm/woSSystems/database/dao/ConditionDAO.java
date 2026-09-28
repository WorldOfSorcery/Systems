package me.hektortm.woSSystems.database.dao;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.hektortm.woSSystems.content.ContentRegistry;
import me.hektortm.woSSystems.content.ContentStore;
import me.hektortm.woSSystems.content.Json;
import me.hektortm.woSSystems.utils.model.Condition;
import me.hektortm.woSSystems.utils.types.ConditionType;
import me.hektortm.wosCore.api.ApiException;
import me.hektortm.wosCore.api.WosApi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Conditions attached to content children (interaction actions/particles/holograms,
 * GUI slot configs, dialog answers, recipes), indexed by {@code "<type>:<type_id>"}.
 *
 * <p>Checked on every tick for every active entity, so lookups are pure map reads.
 * All conditions are loaded at startup; afterwards the interaction, GUI and dialog
 * stores replace their own children's conditions whenever they reload (their API
 * trees embed them), and wos-api invalidates the parent on every condition edit.</p>
 */
public class ConditionDAO {
    private final ContentStore<List<Condition>> store;

    public ConditionDAO(ContentRegistry registry, WosApi api) {
        this.store = registry.register(new ContentStore<>("conditions", "Condition", new ContentStore.Source<>() {
            @Override
            public Map<String, List<Condition>> loadAll() throws ApiException {
                return group(api.getJson("/v1/content/conditions").getAsJsonArray());
            }

            @Override
            public Optional<List<Condition>> loadOne(String key) throws ApiException {
                String[] parts = key.split(":", 2); // "<type>:<type_id>"
                if (parts.length < 2) return Optional.empty();
                List<Condition> list = group(api.getJson("/v1/content/conditions/" + parts[0] + "/" + parts[1]).getAsJsonArray())
                        .getOrDefault(key, List.of());
                return list.isEmpty() ? Optional.empty() : Optional.of(list);
            }
        }));
    }

    /** Conditions for one child; empty if none. */
    public List<Condition> getConditions(ConditionType type, String id) {
        List<Condition> list = store.get(type.getType() + ":" + id);
        return list == null ? List.of() : list;
    }

    /**
     * Replaces the conditions of every child of {@code parentId} for the given
     * condition types with those embedded in a freshly loaded parent tree.
     */
    public void replaceChildren(Set<String> types, String parentId, JsonArray conditions) {
        store.replaceMatching(key -> {
            int colon = key.indexOf(':');
            return colon > 0 && types.contains(key.substring(0, colon))
                    && key.substring(colon + 1).startsWith(parentId + ":");
        }, group(conditions));
    }

    /** Groups API condition rows by "<type>:<type_id>". */
    static Map<String, List<Condition>> group(JsonArray rows) {
        Map<String, List<Condition>> out = new HashMap<>();
        for (JsonElement el : rows) {
            JsonObject c = el.getAsJsonObject();
            out.computeIfAbsent(Json.str(c, "type") + ":" + Json.str(c, "type_id"), k -> new ArrayList<>())
                    .add(toCondition(c));
        }
        return out;
    }

    static Condition toCondition(JsonObject c) {
        return new Condition(Json.str(c, "condition_key"), Json.str(c, "value"), Json.str(c, "parameter"));
    }
}
