package me.hektortm.woSSystems.systems.quests.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Represents a single node in a quest flowchart.
 *
 * <p>Node types: {@code start}, {@code dialog}, {@code parallel},
 * {@code objective}, {@code reward}, {@code end}.</p>
 */
public class QuestNode {

    private final String id;
    private final String type;
    private final JsonObject data;

    public QuestNode(String id, String type, JsonObject data) {
        this.id   = id;
        this.type = type;
        this.data = data != null ? data : new JsonObject();
    }

    public String     getId()   { return id;   }
    public String     getType() { return type; }
    public JsonObject getData() { return data; }

    public String getLabel() {
        return data.has("label") ? data.get("label").getAsString() : id;
    }

    // ---- dialog node ----

    /** Returns the dialog_id for dialog nodes, or {@code null} / empty if unset. */
    public String getDialogId() {
        if (!data.has("dialog_id")) return null;
        String v = data.get("dialog_id").getAsString();
        return v.isEmpty() ? null : v;
    }

    // ---- objective node ----

    public JsonObject getObjectiveData() {
        return data.has("objective") ? data.getAsJsonObject("objective") : null;
    }

    public String getObjectiveType() {
        JsonObject o = getObjectiveData();
        return (o != null && o.has("type")) ? o.get("type").getAsString() : null;
    }

    public String getObjectiveLabel() {
        JsonObject o = getObjectiveData();
        return (o != null && o.has("label")) ? o.get("label").getAsString() : getLabel();
    }

    public int getObjectiveQuantity() {
        JsonObject o = getObjectiveData();
        return (o != null && o.has("quantity")) ? o.get("quantity").getAsInt() : 1;
    }

    /** Returns the target entity / item ID for kill or collect objectives, or {@code null}. */
    public String getObjectiveTargetId() {
        JsonObject o = getObjectiveData();
        return (o != null && o.has("target_id")) ? o.get("target_id").getAsString() : null;
    }

    // ---- reward node ----

    public JsonArray getRewards() {
        return data.has("rewards") ? data.getAsJsonArray("rewards") : new JsonArray();
    }

    // ---- end node ----

    public boolean isSuccess() {
        return !data.has("success") || data.get("success").getAsBoolean();
    }

    public String getEndMessage() {
        return data.has("message") ? data.get("message").getAsString() : null;
    }

    // ---- parallel node ----

    public int getBranchCount() {
        return data.has("branch_count") ? data.get("branch_count").getAsInt() : 0;
    }
}
