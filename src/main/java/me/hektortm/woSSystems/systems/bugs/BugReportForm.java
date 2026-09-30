package me.hektortm.woSSystems.systems.bugs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * What a player filled in on the /bug dialog: the answers, what's still
 * wrong with them, and the report as wos-api's body. Players describe the bug
 * in their own words; they aren't asked which system it's in (staff sort
 * that in the portal). No Bukkit here: {@link BugCommand} builds the dialog.
 */
final class BugReportForm {

    /** value → label (the API stores the value, the dialog shows the label). */
    static final Map<String, String> SEVERITIES = ordered(
            "blocker", "I can't play", "major", "Something doesn't work",
            "minor", "It works, but not right", "cosmetic", "It only looks wrong");
    static final Map<String, String> FREQUENCIES = ordered(
            "always", "Every time", "sometimes", "Sometimes", "once", "It happened once");

    static final int MAX_TITLE = 120;
    static final int MAX_TEXT = 1000;

    // The answers ("" = not given).
    String title = "";
    String what = "";
    String steps = "";
    String severity = "";
    String frequency = "";

    /** What's still missing or wrong, as short phrases (empty: ready to send). */
    List<String> problems() {
        List<String> out = new ArrayList<>();
        if (title.isBlank()) out.add("give it a short title");
        else if (title.strip().length() > MAX_TITLE) out.add("the title is too long");
        if (what.isBlank()) out.add("say what happened");
        if (!SEVERITIES.containsKey(severity)) out.add("pick how bad it is");
        if (!FREQUENCIES.containsKey(frequency)) out.add("pick how often it happens");
        return out;
    }

    /** The report as the API's /v1/server/bugs body (without the player). No feature: staff set it. */
    Map<String, Object> body(String location) {
        Map<String, Object> b = new LinkedHashMap<>();
        b.put("area", "minecraft");
        b.put("title", title.strip());
        b.put("what_happened", what.strip());
        b.put("steps", steps.strip());
        b.put("severity", severity);
        b.put("frequency", frequency);
        b.put("location", location);
        return b;
    }

    private static Map<String, String> ordered(String... kv) {
        Map<String, String> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) m.put(kv[i], kv[i + 1]);
        return Collections.unmodifiableMap(m);
    }
}
