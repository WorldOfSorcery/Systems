package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.content.ApiSource;
import me.hektortm.woSSystems.content.ContentRegistry;
import me.hektortm.woSSystems.content.ContentStore;
import me.hektortm.woSSystems.content.Json;
import me.hektortm.woSSystems.utils.model.Activity;
import me.hektortm.wosCore.api.WosApi;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/** Scheduled time events ("activities"), served from {@code /v1/content/timeevents}. */
public class TimeDAO {
    private final ContentStore<Activity> store;

    public TimeDAO(ContentRegistry registry, WosApi api, Logger log) {
        this.store = registry.register(new ContentStore<>("timeevents", "Time Event",
                ApiSource.flat(api, "/v1/content/timeevents", "id", j -> new Activity(
                        Json.str(j, "id"),
                        Json.bool(j, "is_enabled", false),
                        Json.str(j, "name"),
                        Json.str(j, "message"),
                        Json.bool(j, "is_default", false),
                        Json.str(j, "date"),
                        Json.integer(j, "start_time", 0),
                        Json.integer(j, "end_time", 0),
                        Json.str(j, "start_interaction"),
                        Json.str(j, "end_interaction")), log)));
    }

    /** Every time-event definition. */
    public List<Activity> getAllActivities() {
        return new ArrayList<>(store.all());
    }
}
