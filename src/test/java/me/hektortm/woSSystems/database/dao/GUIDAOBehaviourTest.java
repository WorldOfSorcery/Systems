package me.hektortm.woSSystems.database.dao;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.hektortm.woSSystems.utils.model.GUIItemBehaviour;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** How a slot config's JSON from wos-api becomes its {@link GUIItemBehaviour}. */
class GUIDAOBehaviourTest {

    private static JsonObject json(String s) {
        return JsonParser.parseString(s).getAsJsonObject();
    }

    @Test
    void readsEveryField() {
        GUIItemBehaviour b = GUIDAO.behaviour(json("""
                {"head_texture":"https://t/abc","citem_id":"bass","cost_currency":"gold","cost_amount":25,"show_cost":true,
                 "cooldown_id":"shop_buy","cooldown_actions":["send_message wait"],
                 "shift_left_actions":["c"],"shift_right_actions":["a"],"drop_actions":["b"],
                 "trade":{"take":[{"type":"citem","id":"bass","amount":3}],"give":[{"type":"currency","id":"gold","amount":20}],"show":true},
                 "clickable":false,"post_use":"gui","post_use_target":"bank","citem_name":false,"citem_lore":"both"}"""));
        assertThat(b.headTexture()).isEqualTo("https://t/abc");
        assertThat(b.citemId()).isEqualTo("bass");
        assertThat(b.hasCost()).isTrue();
        assertThat(b.costAmount()).isEqualTo(25);
        assertThat(b.showCost()).isTrue();
        assertThat(b.cooldownId()).isEqualTo("shop_buy");
        assertThat(b.cooldownActions()).containsExactly("send_message wait");
        assertThat(b.shiftLeftActions()).containsExactly("c");
        assertThat(b.shiftRightActions()).containsExactly("a");
        assertThat(b.dropActions()).containsExactly("b");
        assertThat(b.trade().take()).containsExactly(new GUIItemBehaviour.Entry("citem", "bass", 3));
        assertThat(b.trade().give()).containsExactly(new GUIItemBehaviour.Entry("currency", "gold", 20));
        assertThat(b.trade().show()).isTrue();
        assertThat(b.citemName()).isFalse();
        assertThat(b.citemLore()).isEqualTo("both");
        assertThat(b.clickable()).isFalse();
        assertThat(b.postUse()).isEqualTo("gui");
        assertThat(b.postUseTarget()).isEqualTo("bank");
    }

    @Test
    void aConfigSavedBeforeTheFeaturesIsPlain() {
        GUIItemBehaviour b = GUIDAO.behaviour(json("{\"material\":\"STONE\"}"));
        assertThat(b).isEqualTo(GUIItemBehaviour.PLAIN);
    }

    @Test
    void blankValuesAndBrokenTradeEntriesAreDropped() {
        GUIItemBehaviour b = GUIDAO.behaviour(json("""
                {"head_texture":"  ","cooldown_id":"","post_use_target":"",
                 "trade":{"take":[{"type":"citem","id":"","amount":1},{"type":"citem","id":"x","amount":0},"junk"],"give":[]}}"""));
        assertThat(b.headTexture()).isNull();
        assertThat(b.cooldownId()).isNull();
        assertThat(b.postUseTarget()).isNull();
        assertThat(b.trade().take()).isEqualTo(List.of());
    }
}
