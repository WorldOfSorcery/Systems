package me.hektortm.woSSystems.systems.bugs;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BugReportFormTest {

    private static BugReportForm filled() {
        BugReportForm f = new BugReportForm();
        f.title = "  Shop GUI closes ";
        f.what = "Clicking the sword closes it.";
        f.steps = "open /shop\nclick the sword\n";
        f.severity = "major";
        f.frequency = "always";
        return f;
    }

    @Test
    void aCompleteFormBuildsTheReport() {
        BugReportForm f = filled();
        assertThat(f.problems()).isEmpty();
        assertThat(f.body("world 1 64 -2")).doesNotContainKey("feature") // players don't pick one: staff sort it
                .containsEntry("area", "minecraft")
                .containsEntry("title", "Shop GUI closes").containsEntry("steps", "open /shop\nclick the sword")
                .containsEntry("severity", "major").containsEntry("frequency", "always").containsEntry("location", "world 1 64 -2");
    }

    @Test
    void listsWhatIsMissing() {
        assertThat(new BugReportForm().problems()).containsExactly(
                "give it a short title", "say what happened", "pick how bad it is", "pick how often it happens");
    }

    @Test
    void refusesTooLongAnswers() {
        BugReportForm f = filled();
        f.title = "x".repeat(121);
        assertThat(f.problems()).containsExactly("the title is too long");
    }

    @Test
    void dropdownIdsMapBackToTheirValues() {
        List<String> severities = List.copyOf(BugReportForm.SEVERITIES.keySet());
        assertThat(BugCommand.optionId("Some Thing & more")).isEqualTo("some_thing_more");
        assertThat(BugCommand.fromOptionId(severities, "major")).isEqualTo("major");
        assertThat(BugCommand.fromOptionId(severities, "none")).isEmpty(); // "pick one"
        assertThat(BugCommand.fromOptionId(severities, null)).isEmpty();
    }
}
