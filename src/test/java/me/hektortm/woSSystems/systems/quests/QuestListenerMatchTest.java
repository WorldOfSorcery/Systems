package me.hektortm.woSSystems.systems.quests;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link QuestListener#matchesTarget(String, String, String)} — the pure
 * predicate that decides whether a killed entity / used item satisfies a quest objective's
 * target. Covers the null/empty wildcard, type-name match, and display-name normalisation
 * (spaces → underscores, case-insensitive) branches.
 */
class QuestListenerMatchTest {

    @Test
    void nullOrEmptyTargetMatchesAnything() {
        assertThat(QuestListener.matchesTarget(null, "zombie", "Boss Zombie")).isTrue();
        assertThat(QuestListener.matchesTarget("", "zombie", null)).isTrue();
    }

    @Test
    void matchesOnTypeNameCaseInsensitively() {
        assertThat(QuestListener.matchesTarget("ZOMBIE", "zombie", null)).isTrue();
        assertThat(QuestListener.matchesTarget("zombie", "zombie", null)).isTrue();
    }

    @Test
    void matchesDisplayNameWithSpacesOrUnderscores() {
        // Underscored form of the display name.
        assertThat(QuestListener.matchesTarget("boss_zombie", "zombie", "Boss Zombie")).isTrue();
        // Raw (spaced) form of the display name.
        assertThat(QuestListener.matchesTarget("boss zombie", "zombie", "Boss Zombie")).isTrue();
        // Target itself is upper-cased; matching is case-insensitive on both sides.
        assertThat(QuestListener.matchesTarget("Boss_Zombie", "zombie", "Boss Zombie")).isTrue();
    }

    @Test
    void rejectsNonMatchingTarget() {
        assertThat(QuestListener.matchesTarget("skeleton", "zombie", "Boss Zombie")).isFalse();
        // No type match and no display name to fall back on.
        assertThat(QuestListener.matchesTarget("anything", "zombie", null)).isFalse();
    }
}
