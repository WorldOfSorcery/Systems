package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.content.ApiSource;
import me.hektortm.woSSystems.content.ContentRegistry;
import me.hektortm.woSSystems.content.ContentStore;
import me.hektortm.woSSystems.content.Json;
import me.hektortm.woSSystems.utils.model.BasicCommand;
import me.hektortm.wosCore.api.WosApi;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/** Custom chat commands bound to an interaction ({@code /v1/content/commands}). */
public class CommandsDAO {
    private final ContentStore<BasicCommand> store;

    public CommandsDAO(ContentRegistry registry, WosApi api, Logger log) {
        this.store = registry.register(new ContentStore<>("commands", "Command",
                ApiSource.flat(api, "/v1/content/commands", "command",
                        j -> new BasicCommand(Json.str(j, "command"), Json.str(j, "interaction"), Json.str(j, "permission")),
                        log)));
    }

    /** Every custom command definition. */
    public List<BasicCommand> getCommands() {
        return new ArrayList<>(store.all());
    }
}
