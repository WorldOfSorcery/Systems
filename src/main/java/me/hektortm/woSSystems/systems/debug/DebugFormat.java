package me.hektortm.woSSystems.systems.debug;

import org.jetbrains.annotations.Nullable;

/**
 * The texts debug mode shows, without touching the server: the chat lines about
 * a list of commands that ran, the line added to a GUI item's lore, and the
 * label above a bound interaction.
 */
public final class DebugFormat {

    static final String PREFIX = "§8[debug] ";
    private static final String SEPARATOR = " · ";

    private DebugFormat() {}

    /**
     * The line above a list of commands: where they come from, e.g.
     * {@code interaction shop_keeper · row 2 · npc:12}. Empty details are left out.
     */
    public static String header(String source, String id, @Nullable String... details) {
        StringBuilder out = new StringBuilder(PREFIX).append("§e").append(source).append(' ').append(id).append("§7");
        if (details != null) {
            for (String detail : details) {
                if (detail != null && !detail.isBlank()) out.append(SEPARATOR).append(detail);
            }
        }
        return out.toString();
    }

    /**
     * One command: as written, and as it ran if filling in the placeholders
     * and {@code @p} changed it. Colour codes are shown, not applied.
     */
    public static String command(String written, String run) {
        String line = PREFIX + "§7  " + visible(written);
        return written.equals(run) ? line : line + " §8→ §f" + visible(run);
    }

    /** A remark under a header: a skipped row, where a click led … */
    public static String note(String text) {
        return PREFIX + "§7  " + text;
    }

    /**
     * One condition of a row and whether the player meets it: green ✔ or red ✘,
     * the condition as set up, and what it found for the player if known, e.g.
     * {@code ✘ has_stats_greater_than kills 10 (is 6)}.
     */
    public static String condition(String name, @Nullable String value, @Nullable String parameter, boolean met, @Nullable String actual) {
        StringBuilder out = new StringBuilder(PREFIX).append(met ? "§a  ✔ " : "§c  ✘ ").append(name);
        if (value != null && !value.isBlank()) out.append(' ').append(value);
        if (parameter != null && !parameter.isBlank()) out.append(' ').append(parameter);
        if (actual != null && !actual.isBlank()) out.append(" §7(").append(actual).append(')');
        return out.toString();
    }

    /** The lore line of a GUI item: which slot it is and which of the slot's configs shows. */
    public static String guiItem(int slot, String config) {
        return "§8slot " + slot + SEPARATOR + "config " + config;
    }

    /** The label above a bound block / NPC: the interaction and what it is bound to. */
    public static String label(String interactionId, String binding) {
        return "§e" + interactionId + "\n§7" + binding;
    }

    private static String visible(String text) {
        return text.replace('§', '&');
    }
}
