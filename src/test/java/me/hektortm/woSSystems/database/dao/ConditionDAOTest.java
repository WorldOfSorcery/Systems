package me.hektortm.woSSystems.database.dao;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import me.hektortm.woSSystems.content.ContentRegistry;
import me.hektortm.woSSystems.utils.model.Condition;
import me.hektortm.woSSystems.utils.types.ConditionType;
import me.hektortm.wosCore.api.WosApi;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConditionDAOTest {

    private static JsonArray rows(String json) {
        return JsonParser.parseString(json).getAsJsonArray();
    }

    private static ConditionDAO dao() {
        Plugin plugin = mock(Plugin.class);
        when(plugin.getLogger()).thenReturn(Logger.getLogger("test"));
        WosApi api = new WosApi("http://127.0.0.1:1", "t", Duration.ofMillis(100), Logger.getLogger("test"));
        return new ConditionDAO(new ContentRegistry(plugin), api);
    }

    @Test
    void groupsRowsByTypeAndTypeId() {
        var grouped = ConditionDAO.group(rows("""
                [{"type":"interaction","type_id":"shop:1","condition_key":"has_unlockable","value":"vip","parameter":null},
                 {"type":"interaction","type_id":"shop:1","condition_key":"has_stat","value":"coins","parameter":"10"},
                 {"type":"guislot","type_id":"menu:0:4:0","condition_key":"has_unlockable","value":"x","parameter":null}]"""));

        assertThat(grouped).containsOnlyKeys("interaction:shop:1", "guislot:menu:0:4:0");
        assertThat(grouped.get("interaction:shop:1")).hasSize(2);
    }

    @Test
    void replaceChildrenRefreshesOnlyThatParentsConditions() {
        ConditionDAO dao = dao();
        dao.replaceChildren(Set.of("interaction", "particle", "hologram"), "shop", rows("""
                [{"type":"interaction","type_id":"shop:1","condition_key":"old","value":"1","parameter":null},
                 {"type":"particle","type_id":"shop:2","condition_key":"old","value":"1","parameter":null}]"""));
        dao.replaceChildren(Set.of("interaction"), "shopkeeper", rows("""
                [{"type":"interaction","type_id":"shopkeeper:1","condition_key":"other","value":"1","parameter":null}]"""));

        // shop is edited: its particle condition was removed, its action condition changed
        dao.replaceChildren(Set.of("interaction", "particle", "hologram"), "shop", rows("""
                [{"type":"interaction","type_id":"shop:1","condition_key":"new","value":"2","parameter":null}]"""));

        List<Condition> action = dao.getConditions(ConditionType.INTERACTION, "shop:1");
        assertThat(action).hasSize(1);
        assertThat(dao.getConditions(ConditionType.PARTICLE, "shop:2")).isEmpty();
        // "shopkeeper" shares the "shop" prefix but is a different parent — untouched
        assertThat(dao.getConditions(ConditionType.INTERACTION, "shopkeeper:1")).hasSize(1);
    }
}
