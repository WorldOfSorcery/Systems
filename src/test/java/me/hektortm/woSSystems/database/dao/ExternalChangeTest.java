package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.database.dao.EconomyDAO.ExternalChange;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** The "a balance changed in the portal" message from wos-api. */
class ExternalChangeTest {
    private static final String UUID_TEXT = "28c63f65-52cc-47f0-8246-b16f2176da23";

    @Test
    void readsWhoWhatAndByHowMuch() {
        ExternalChange c = ExternalChange.parse(UUID_TEXT + "|gold|-250|4711");
        assertThat(c).isEqualTo(new ExternalChange(UUID.fromString(UUID_TEXT), "gold", -250, 4711));
    }

    @Test
    void anythingElseIsRefused() {
        for (String bad : new String[]{null, "", "gold", UUID_TEXT + "|gold|10", UUID_TEXT + "||10|1",
                "not-a-uuid|gold|10|1", UUID_TEXT + "|gold|ten|1", UUID_TEXT + "|gold|10|1|extra"}) {
            assertThat(ExternalChange.parse(bad)).as("%s", bad).isNull();
        }
    }
}
