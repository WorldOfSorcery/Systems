package me.hektortm.woSSystems.utils.model;

/**
 * Immutable triple representing a single named condition used by the condition
 * evaluation system.
 *
 * <p>A condition is evaluated at runtime by
 * {@link me.hektortm.woSSystems.utils.ConditionHandler#evaluate} against a
 * player's current state.  The three fields correspond to the condition
 * type ({@link #name}), the primary subject ({@link #value}), and an optional
 * secondary constraint ({@link #parameter}).</p>
 *
 * <p>Example: {@code name="has_stats_greater_than"}, {@code value="kills"},
 * {@code parameter="10"} — evaluates to {@code true} if the player's
 * {@code kills} stat exceeds 10.</p>
 */
public class Condition {
    private final String name;       // e.g., "hasItem", "levelMin"
    private final String value;      // e.g., "diamond_sword", "10"
    private final String parameter;  // optional: maybe "slot", "world", etc.

    /**
     * @param name      the condition type name (e.g. {@code "has_citem"})
     * @param value     the primary subject (e.g. an item ID, stat name, or region name)
     * @param parameter an optional secondary constraint (e.g. a minimum amount or world name)
     */
    public Condition(String name, String value, String parameter) {
        this.name = name;
        this.value = value;
        this.parameter = parameter;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getParameter() {
        return parameter;
    }
}

