package me.hektortm.woSSystems.utils;

import me.hektortm.woSSystems.utils.ClickableMessage.Kind;
import me.hektortm.woSSystems.utils.ClickableMessage.Link;
import me.hektortm.woSSystems.utils.ClickableMessage.Part;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Unit tests for {@link ClickableMessage}: the clickable parts of a send_message. */
class ClickableMessageTest {

    @Test
    void aMessageWithoutALinkIsOnePieceOfText() {
        assertThat(ClickableMessage.parse("&aHello [VIP] (really)")).containsExactly(new Part("&aHello [VIP] (really)", null));
        assertThat(ClickableMessage.hasLinks("&aHello [VIP] (really)")).isFalse();
        assertThat(ClickableMessage.parse("")).containsExactly(new Part("", null));
    }

    @Test
    void aLinkSplitsTheMessage() {
        assertThat(ClickableMessage.parse("&aDone, [&eclick here](interaction:daily_rewards)&a to get your rewards")).containsExactly(
                new Part("&aDone, ", null),
                new Part("&eclick here", new Link(Kind.INTERACTION, "daily_rewards", false)),
                new Part("&a to get your rewards", null));
        assertThat(ClickableMessage.hasLinks("[x](url:https://example.com)")).isTrue();
    }

    @Test
    void anActionKeepsItsWholeCommand() {
        assertThat(ClickableMessage.parse("[Shop](action:gui open @p shop:2)")).containsExactly(
                new Part("Shop", new Link(Kind.ACTION, "gui open @p shop:2", false)));
    }

    @Test
    void repeatLetsItBeClickedAgain() {
        assertThat(ClickableMessage.parse("[Menu](repeat:interaction:menu)")).containsExactly(
                new Part("Menu", new Link(Kind.INTERACTION, "menu", true)));
    }

    @Test
    void severalLinksEachKeepTheirTarget() {
        assertThat(ClickableMessage.parse("[A](interaction:a) or [B](url:https://b.example)")).containsExactly(
                new Part("A", new Link(Kind.INTERACTION, "a", false)),
                new Part(" or ", null),
                new Part("B", new Link(Kind.URL, "https://b.example", false)));
    }

    @Test
    void bracketsWithoutAKnownTargetStayText() {
        assertThat(ClickableMessage.hasLinks("[x](something:else)")).isFalse();
        assertThat(ClickableMessage.hasLinks("[x](interaction:)")).isFalse();
        assertThat(ClickableMessage.hasLinks("[x] (interaction:a)")).isFalse();
    }
}
