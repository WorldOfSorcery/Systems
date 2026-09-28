package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.content.ApiSource;
import me.hektortm.woSSystems.content.ContentRegistry;
import me.hektortm.woSSystems.content.ContentStore;
import me.hektortm.woSSystems.content.Json;
import me.hektortm.woSSystems.utils.model.FishingItem;
import me.hektortm.wosCore.api.WosApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

/**
 * Fishing loot definitions ({@code /v1/content/fishing}). A catch is picked
 * entirely from memory — the legacy DAO queried MySQL on every cast.
 */
public class FishingDAO {
    private final ContentStore<FishingItem> store;

    public FishingDAO(ContentRegistry registry, WosApi api, Logger log) {
        this.store = registry.register(new ContentStore<>("fishing", "Fish",
                ApiSource.flat(api, "/v1/content/fishing", "id", j -> new FishingItem(
                        Json.str(j, "id"),
                        Json.str(j, "citem_id"),
                        Json.str(j, "catch_interaction"),
                        parseRegions(Json.str(j, "regions")),
                        Json.str(j, "rarity")), log)));
    }

    /**
     * A random item of {@code rarity} that may be caught in {@code region}. Items
     * with no regions are eligible everywhere. {@code null} if none qualify.
     */
    public FishingItem getRandomItemByRarityAndRegion(String rarity, String region) {
        List<FishingItem> eligible = new ArrayList<>();
        for (FishingItem item : store.all()) {
            if (!rarity.equalsIgnoreCase(item.getRarity())) continue;
            List<String> regions = item.getRegions();
            if (regions.isEmpty() || regions.contains(region)) eligible.add(item);
        }
        return eligible.isEmpty() ? null : eligible.get(ThreadLocalRandom.current().nextInt(eligible.size()));
    }

    /** "a, b" / "\"a\",\"b\"" → [a, b]; blank → []. */
    private static List<String> parseRegions(String raw) {
        List<String> out = new ArrayList<>();
        if (raw == null || raw.isBlank()) return out;
        for (String r : Arrays.asList(raw.replace("\"", "").split(","))) {
            if (!r.isBlank()) out.add(r.trim());
        }
        return out;
    }
}
