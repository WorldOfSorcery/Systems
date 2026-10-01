package me.hektortm.woSSystems.utils.model;

import me.hektortm.woSSystems.database.annotation.Column;
import me.hektortm.woSSystems.database.annotation.Table;

/**
 * Immutable definition of an in-game currency managed by the economy system.
 *
 * <p>Maps to the {@code currencies} database table.  Player balances are stored
 * in a separate player-data table and accessed via
 * {@link me.hektortm.woSSystems.database.dao.EconomyDAO}.</p>
 */
@Table("currencies")
public class Currency extends BaseEntity {

    @Column(notNull = true)
    private final String name;

    @Column
    private final String icon;

    @Column
    private final String color;

    @Column(name = "hidden_if_zero", defaultValue = "FALSE")
    private final boolean hiddenIfZero;

    /** Whether players may /pay it to each other. */
    private final boolean payable;

    /** The highest balance a player can have; 0 = no limit. */
    private final long maxBalance;

    /**
     * @param id           the unique currency ID
     * @param name         the display name
     * @param icon         an optional icon string (e.g. a Unicode symbol or item key)
     * @param color        an optional colour as staff type it ("&6", "§6", "#FFAA00", "&#FFAA00"); see {@link #colorCode}
     * @param hiddenIfZero when {@code true}, the currency is omitted from UI if the balance is zero
     */
    public Currency(String id, String name, String icon, String color, boolean hiddenIfZero) {
        this(id, name, icon, color, hiddenIfZero, true, 0);
    }

    /**
     * @param payable    whether players may /pay it to each other
     * @param maxBalance the highest balance a player can have; 0 (or less) = no limit
     */
    public Currency(String id, String name, String icon, String color, boolean hiddenIfZero,
                    boolean payable, long maxBalance) {
        super(id);
        this.payable      = payable;
        this.maxBalance   = Math.max(0, maxBalance);
        this.name         = name;
        this.icon         = icon;
        this.color        = colorCode(color);
        this.hiddenIfZero = hiddenIfZero;
    }

    /**
     * A colour as typed in the portal, made ready to put in front of text in a
     * message: "&6" and "&l" become "§6" / "§l", "&#FFAA00" becomes "#FFAA00"
     * (messages understand § codes and #RRGGBB, not &). No colour is "", never
     * null, so it can be concatenated safely.
     */
    public static String colorCode(String raw) {
        if (raw == null) return "";
        String c = raw.trim();
        StringBuilder out = new StringBuilder(c.length());
        for (int i = 0; i < c.length(); i++) {
            char ch = c.charAt(i);
            if (ch == '&' && i + 1 < c.length()) {
                char next = c.charAt(i + 1);
                if (next == '#') continue; // "&#RRGGBB": the hex code follows
                if ("0123456789abcdefklmnorABCDEFKLMNOR".indexOf(next) >= 0) {
                    out.append('§');
                    continue;
                }
            }
            out.append(ch);
        }
        return out.toString();
    }

    public String  getName()        { return name;         }
    public String  getIcon()        { return icon;         }
    /** The colour, ready to put in front of text ("" when the currency has none). */
    public String  getColor()       { return color;        }
    public boolean isHiddenIfZero() { return hiddenIfZero; }
    public boolean isPayable()      { return payable;      }
    public long    getMaxBalance()  { return maxBalance;   }

    /** A balance kept within this currency's limits: never below 0, never above the maximum. */
    public long clamp(long balance) {
        long floor = Math.max(0, balance);
        return maxBalance > 0 ? Math.min(floor, maxBalance) : floor;
    }
}
