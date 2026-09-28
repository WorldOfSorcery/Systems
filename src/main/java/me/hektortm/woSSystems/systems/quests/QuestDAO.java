package me.hektortm.woSSystems.systems.quests;

import com.google.gson.*;
import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.content.ApiSource;
import me.hektortm.woSSystems.content.ContentStore;
import me.hektortm.woSSystems.content.Json;
import me.hektortm.woSSystems.player.ApiServices;
import me.hektortm.woSSystems.player.ApiWriter;
import me.hektortm.woSSystems.player.PlayerSession;
import me.hektortm.woSSystems.player.PlayerSessions;
import me.hektortm.woSSystems.systems.quests.model.PlayerQuestState;
import me.hektortm.woSSystems.systems.quests.model.Quest;
import me.hektortm.woSSystems.systems.quests.model.QuestEdge;
import me.hektortm.woSSystems.systems.quests.model.QuestNode;

import java.util.*;

import static me.hektortm.woSSystems.player.ApiWriter.body;
import static me.hektortm.woSSystems.player.ApiWriter.seg;

/**
 * DAO for the quest system: definitions from {@code /v1/content/quests}; each
 * player's quest progress and completion history live in their
 * {@link PlayerSession} and are written through to the API.
 */
public class QuestDAO {

    private static final Gson GSON = new Gson();

    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final PlayerSessions sessions;
    private final ApiWriter writer;

    /** Quest definition cache: id → Quest */
    private final ContentStore<Quest> questCache;

    public QuestDAO(ApiServices s) {
        this.sessions = s.sessions();
        this.writer = s.writer();
        this.questCache = s.content().register(new ContentStore<>("quests", "Quest",
                ApiSource.tree(s.api(), "/v1/content/quests", this::buildQuest, s.log())));
    }

    // -------------------------------------------------------------------------
    // Quest definitions
    // -------------------------------------------------------------------------

    public Quest            getQuest(String id)   { return questCache.get(id); }
    public Collection<Quest> getAllQuests()        { return questCache.all();   }

    // -------------------------------------------------------------------------
    // Player progress
    // -------------------------------------------------------------------------

    /** The player's active quest states (from their session). */
    public List<PlayerQuestState> loadPlayerStates(UUID uuid) {
        List<PlayerQuestState> states = new ArrayList<>();
        PlayerSession s = sessions.getOrFetch(uuid);
        if (s == null) return states;
        for (JsonObject q : s.quests.values()) {
            if ("active".equals(Json.str(q, "status", "active"))) states.add(buildState(uuid, q));
        }
        return states;
    }

    /** Saves a player's quest state (session + API). */
    public void savePlayerState(PlayerQuestState state) {
        JsonObject doc = new JsonObject();
        doc.addProperty("quest_id", state.getQuestId());
        doc.addProperty("status", state.getStatus());
        doc.add("active_nodes", GSON.toJsonTree(state.getActiveNodes()));
        doc.add("completed_nodes", GSON.toJsonTree(state.getCompletedNodes()));
        doc.add("objective_progress", GSON.toJsonTree(state.getObjectiveProgress()));
        doc.add("variables", GSON.toJsonTree(state.getVariables()));
        doc.add("merge_progress", GSON.toJsonTree(state.getMergeProgress()));

        PlayerSession s = sessions.get(state.getUuid());
        if (s != null) s.quests.put(state.getQuestId(), doc);
        writer.put(questPath(state.getUuid(), state.getQuestId()), doc);
    }

    /** Records a quest completion in the history. */
    public void recordCompletion(UUID uuid, String questId, boolean success) {
        PlayerSession s = sessions.get(uuid);
        if (s != null && success) s.completedQuests.add(questId);
        writer.post(questPath(uuid, questId) + "/completions", body("success", success));
    }

    /** @return {@code true} if the player has ever completed the quest successfully. */
    public boolean hasCompleted(UUID uuid, String questId) {
        PlayerSession s = sessions.getOrFetch(uuid);
        return s != null && s.completedQuests.contains(questId);
    }

    private static String questPath(UUID uuid, String questId) {
        return "/v1/players/" + uuid + "/quests/" + seg(questId);
    }

    // -------------------------------------------------------------------------
    // Builders
    // -------------------------------------------------------------------------

    private Quest buildQuest(JsonObject j) {
        String nJson = Json.str(j, "nodes");
        String eJson = Json.str(j, "edges");
        if (nJson == null || eJson == null) return null;
        return new Quest(Json.str(j, "id"), Json.str(j, "title"), Json.str(j, "description"), parseNodes(nJson), parseEdges(eJson));
    }

    private List<QuestNode> parseNodes(String json) {
        List<QuestNode> result = new ArrayList<>();
        try {
            for (JsonElement el : GSON.fromJson(json, JsonArray.class)) {
                JsonObject obj  = el.getAsJsonObject();
                String id       = obj.get("id").getAsString();
                String type     = obj.has("type") ? obj.get("type").getAsString() : "unknown";
                JsonObject data = obj.has("data") ? obj.getAsJsonObject("data") : new JsonObject();
                result.add(new QuestNode(id, type, data));
            }
        } catch (Exception e) {
            plugin.getLogger().warning("QuestDAO: bad nodes JSON: " + e.getMessage());
        }
        return result;
    }

    private List<QuestEdge> parseEdges(String json) {
        List<QuestEdge> result = new ArrayList<>();
        try {
            for (JsonElement el : GSON.fromJson(json, JsonArray.class)) {
                JsonObject obj = el.getAsJsonObject();
                String id      = obj.get("id").getAsString();
                String source  = obj.get("source").getAsString();
                String target  = obj.get("target").getAsString();
                String handle  = (obj.has("sourceHandle") && !obj.get("sourceHandle").isJsonNull())
                        ? obj.get("sourceHandle").getAsString() : null;
                result.add(new QuestEdge(id, source, target, handle));
            }
        } catch (Exception e) {
            plugin.getLogger().warning("QuestDAO: bad edges JSON: " + e.getMessage());
        }
        return result;
    }

    /** Builds a state from a session quest document (progress fields are JSON values). */
    static PlayerQuestState buildState(UUID uuid, JsonObject q) {
        return new PlayerQuestState(uuid, Json.str(q, "quest_id"), Json.str(q, "status", "active"),
                parseStringSet(Json.str(q, "active_nodes")),
                parseStringSet(Json.str(q, "completed_nodes")),
                parseIntMap(Json.str(q, "objective_progress")),
                parseDoubleMap(Json.str(q, "variables")),
                parseIntMap(Json.str(q, "merge_progress")));
    }

    // -------------------------------------------------------------------------
    // JSON helpers
    // -------------------------------------------------------------------------

    private static Set<String> parseStringSet(String json) {
        Set<String> set = new LinkedHashSet<>();
        try { for (JsonElement e : GSON.fromJson(json, JsonArray.class)) set.add(e.getAsString()); }
        catch (Exception ignored) {}
        return set;
    }

    private static Map<String, Integer> parseIntMap(String json) {
        Map<String, Integer> map = new HashMap<>();
        try {
            for (Map.Entry<String, JsonElement> e : GSON.fromJson(json, JsonObject.class).entrySet())
                map.put(e.getKey(), e.getValue().getAsInt());
        } catch (Exception ignored) {}
        return map;
    }

    private static Map<String, Double> parseDoubleMap(String json) {
        Map<String, Double> map = new HashMap<>();
        try {
            for (Map.Entry<String, JsonElement> e : GSON.fromJson(json, JsonObject.class).entrySet())
                map.put(e.getKey(), e.getValue().getAsDouble());
        } catch (Exception ignored) {}
        return map;
    }
}
