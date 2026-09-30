package me.hektortm.woSSystems.systems.quests;

import com.google.gson.JsonParser;
import me.hektortm.woSSystems.systems.quests.model.PlayerQuestState;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class QuestStateJsonTest {

    @Test
    void buildsStateFromSessionQuestDocument() {
        UUID uuid = UUID.randomUUID();
        PlayerQuestState state = QuestDAO.buildState(uuid, JsonParser.parseString("""
            {"quest_id": "intro", "status": "active",
             "active_nodes": ["a", "b"], "completed_nodes": ["start"],
             "objective_progress": {"a": 3}, "variables": {"x": 1.5}, "merge_progress": {}}
            """).getAsJsonObject());

        assertThat(state.getUuid()).isEqualTo(uuid);
        assertThat(state.getQuestId()).isEqualTo("intro");
        assertThat(state.getActiveNodes()).containsExactly("a", "b");
        assertThat(state.getCompletedNodes()).containsExactly("start");
        assertThat(state.getObjectiveProgress()).containsEntry("a", 3);
        assertThat(state.getVariables()).containsEntry("x", 1.5);
        assertThat(state.getMergeProgress()).isEmpty();
    }

    @Test
    void missingProgressFieldsBecomeEmpty() {
        PlayerQuestState state = QuestDAO.buildState(UUID.randomUUID(),
                JsonParser.parseString("{\"quest_id\": \"q\"}").getAsJsonObject());

        assertThat(state.getStatus()).isEqualTo("active");
        assertThat(state.getActiveNodes()).isEmpty();
        assertThat(state.getVariables()).isEmpty();
    }
}
