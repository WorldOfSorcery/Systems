package me.hektortm.woSSystems.database.dao;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.hektortm.woSSystems.content.ApiSource;
import me.hektortm.woSSystems.content.ContentRegistry;
import me.hektortm.woSSystems.content.ContentStore;
import me.hektortm.woSSystems.content.Json;
import me.hektortm.woSSystems.utils.model.Loottable;
import me.hektortm.woSSystems.utils.model.LoottableItem;
import me.hektortm.woSSystems.utils.types.LoottableItemType;
import me.hektortm.wosCore.api.WosApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * Loot tables and their weighted items ({@code /v1/content/loottables/{id}}).
 * An item resolves to a GUI, interaction, dialog, citem, or command.
 */
public class LoottablesDAO {
    private final ContentStore<Loottable> store;

    public LoottablesDAO(ContentRegistry registry, WosApi api, Logger log) {
        this.store = registry.register(new ContentStore<>("loottables", "Loottable",
                ApiSource.tree(api, "/v1/content/loottables", LoottablesDAO::map, log)));
    }

    private static Loottable map(JsonObject j) {
        String id = Json.str(j, "id");
        List<LoottableItem> items = new ArrayList<>();
        for (JsonElement el : Json.array(j, "items")) {
            JsonObject it = el.getAsJsonObject();
            LoottableItemType type = parseItemType(Json.str(it, "type"));
            if (type == null) {
                throw new IllegalArgumentException("invalid loot item type '" + Json.str(it, "type") + "'");
            }
            items.add(new LoottableItem(Json.integer(it, "weight", 0), type, Json.str(it, "value"), Json.integer(it, "parameter", 0)));
        }
        String name = Json.str(j, "name");
        return new Loottable(id, Json.integer(j, "amount", 0), name != null ? name : id, items);
    }

    /** Number of items awarded per roll; 0 if the table is unknown. */
    public int getAmount(String id) {
        Loottable lt = store.get(id);
        return lt == null ? 0 : lt.getAmount();
    }

    /** Display name (falls back to the id); {@code null} if the table is unknown. */
    public String getName(String id) {
        Loottable lt = store.get(id);
        return lt == null ? null : lt.getName();
    }

    /** The loot table with its items, or {@code null} if unknown or empty (legacy contract). */
    public Loottable getLoottable(String id) {
        Loottable lt = store.get(id);
        return lt == null || lt.getItems().isEmpty() ? null : lt;
    }

    private static LoottableItemType parseItemType(String itemType) {
        if (itemType == null) return null;
        return switch (itemType.trim().toLowerCase(Locale.ROOT)) {
            case "gui" -> LoottableItemType.GUI;
            case "interaction" -> LoottableItemType.INTERACTION;
            case "dialog" -> LoottableItemType.DIALOG;
            case "citem" -> LoottableItemType.CITEM;
            case "command" -> LoottableItemType.COMMAND;
            default -> null;
        };
    }
}
