package me.hektortm.woSSystems.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ActionHandlerArgumentTest {

    @Test
    void takesWhatFollowsTheKeyword() {
        assertThat(ActionHandler.argument("send_message &aHi", "send_message")).isEqualTo("&aHi");
        assertThat(ActionHandler.argument("send_title &6Hi -s &7there", "send_title")).isEqualTo("&6Hi -s &7there");
    }

    @Test
    void keepsTheKeywordWhenItAppearsInTheText() {
        // The old replace("send_message ", "") also cut it out of the message itself.
        assertThat(ActionHandler.argument("send_message type send_message to test", "send_message"))
                .isEqualTo("type send_message to test");
    }

    @Test
    void isEmptyWithoutText() {
        assertThat(ActionHandler.argument("send_message", "send_message")).isEmpty();
        assertThat(ActionHandler.argument("empty_line", "empty_line")).isEmpty();
    }
}
