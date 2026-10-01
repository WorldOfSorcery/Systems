package me.hektortm.woSSystems.systems.economy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.OptionalLong;

/**
 * Money amounts as players type and read them: "1,250,000" on screen, and
 * "1250000", "1,250,000", "10k", "2.5m", "1b" or "all" when typed.
 */
public final class Amounts {

    private Amounts() {}

    /** 1250000 → "1,250,000". */
    public static String format(long amount) {
        return String.format(Locale.US, "%,d", amount);
    }

    /**
     * A typed amount as a whole number, or empty when it isn't one.
     *
     * @param all what "all" / "max" stands for (the player's balance), or a
     *            negative number where "all" makes no sense
     */
    public static OptionalLong parse(String typed, long all) {
        if (typed == null) return OptionalLong.empty();
        String s = typed.trim().toLowerCase(Locale.ROOT).replace(",", "").replace("_", "");
        if (s.isEmpty()) return OptionalLong.empty();
        if (s.equals("all") || s.equals("max")) {
            return all >= 0 ? OptionalLong.of(all) : OptionalLong.empty();
        }

        long unit = switch (s.charAt(s.length() - 1)) {
            case 'k' -> 1_000L;
            case 'm' -> 1_000_000L;
            case 'b' -> 1_000_000_000L;
            default -> 1L;
        };
        String number = unit == 1 ? s : s.substring(0, s.length() - 1);
        try {
            BigDecimal value = new BigDecimal(number).multiply(BigDecimal.valueOf(unit));
            // "2.5k" is 2500; "2.5" (half a coin) is not an amount.
            return OptionalLong.of(value.setScale(0, RoundingMode.UNNECESSARY).longValueExact());
        } catch (NumberFormatException | ArithmeticException notAWholeAmount) {
            return OptionalLong.empty();
        }
    }
}
