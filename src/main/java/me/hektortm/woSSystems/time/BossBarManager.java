package me.hektortm.woSSystems.time;

import me.hektortm.woSSystems.utils.Letters;
import me.hektortm.woSSystems.utils.Letters_bg;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.hektortm.woSSystems.utils.Letters_bg.*;

public class BossBarManager {

    private final Map<UUID, BossBar> playerBars = new HashMap<>();
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("(?i)&?#([A-Fa-f0-9]{6})");
    private static final Pattern LEGACY_COLOR_PATTERN = Pattern.compile("&([0-9a-fk-orA-FK-OR])");

    public void createBossBar(Player p) {
        BossBar timeBar = BossBar.bossBar(
                Component.text(""),
                1.0f,
                BossBar.Color.WHITE,
                BossBar.Overlay.PROGRESS
        );
        timeBar.addViewer(p);
        playerBars.put(p.getUniqueId(), timeBar);
    }

    public void updateBossBar(Player p, String time, String date, String activityName) {
        BossBar bossBar = playerBars.get(p.getUniqueId());

        if (bossBar == null) {
            return;
        }
        // Define the color code for the shadow-free effect (#4e5c24)

        // Apply the color to the text
        String formattedTime = applyColorAndNegativeSpace(time);
        String formattedDate = applyColorAndNegativeSpace( date);
        String formattedActivityName = activityName != null ? applyColorAndNegativeSpace( activityName) : null;


        // If there is an active activity, use its name as the title; otherwise, use the time and date
        String title = (formattedActivityName != null) ? String.format(
                BORDER_LEFT.getLetter()+NEGATIVE_SPACE.getLetter()+CLOCK.getLetter()+ NEGATIVE_SPACE.getLetter() + "%s" + BORDER_RIGHT.getLetter()
                        + "  " +
                        BORDER_LEFT.getLetter() + NEGATIVE_SPACE.getLetter()+CALENDER.getLetter()+ NEGATIVE_SPACE.getLetter() + "%s" + Letters_bg.BORDER_RIGHT.getLetter()
                        + "  " +
                        BORDER_LEFT.getLetter() + NEGATIVE_SPACE.getLetter() + "%s" + Letters_bg.BORDER_RIGHT.getLetter(),
                formattedTime, formattedDate, formattedActivityName) : String.format(
                BORDER_LEFT.getLetter() + NEGATIVE_SPACE.getLetter()+ CLOCK.getLetter()+ NEGATIVE_SPACE.getLetter() + "%s" + Letters_bg.BORDER_RIGHT.getLetter()
                        + "  " +
                        BORDER_LEFT.getLetter() + NEGATIVE_SPACE.getLetter()+CALENDER.getLetter()+ NEGATIVE_SPACE.getLetter() + "%s" + Letters_bg.BORDER_RIGHT.getLetter(),
                formattedTime, formattedDate);

        String rawJson = String.format("""
        [
            {"text":"%s", "shadow_color":0}
        ]
        """, title);

        Component component = GsonComponentSerializer.gson().deserialize(rawJson);
        bossBar.name(component);
    }



    public void removeBossBar(Player p) {
        BossBar bossBar = playerBars.remove(p.getUniqueId());
        if (bossBar != null) {
            bossBar.removeViewer(p);
        }
    }

    private String parseUni(String s) {
        StringBuilder result = new StringBuilder();

        for (char c : s.toCharArray()) {
            Letters_bg letterEnum = null;

            // Check if the character is a letter (uppercase or lowercase)
            if (Character.isLetter(c)) {
                // Try to map both upper and lower case letters
                String letter = String.valueOf(c);  // Keep the case as is (no need to convert)
                try {
                    letterEnum = Letters_bg.valueOf(letter);  // Directly map the letter to the enum
                } catch (IllegalArgumentException e) {
                    // Handle the case where no corresponding enum exists (though we assume all letters are mapped)
                    // For example, this will never happen if all alphabetic characters are defined in your enum
                }
            } else if (Character.isDigit(c)) {
                // Map digits
                switch (c) {
                    case '0': letterEnum = Letters_bg.ZERO; break;
                    case '1': letterEnum = Letters_bg.ONE; break;
                    case '2': letterEnum = Letters_bg.TWO; break;
                    case '3': letterEnum = Letters_bg.THREE; break;
                    case '4': letterEnum = Letters_bg.FOUR; break;
                    case '5': letterEnum = Letters_bg.FIVE; break;
                    case '6': letterEnum = Letters_bg.SIX; break;
                    case '7': letterEnum = Letters_bg.SEVEN; break;
                    case '8': letterEnum = Letters_bg.EIGHT; break;
                    case '9': letterEnum = Letters_bg.NINE; break;
                }
            } else if (c == ':') {
                // Handle colon
                letterEnum = Letters_bg.COLON;
            } else if (c == ' ') {
                letterEnum = Letters_bg.SPACE;
            } else if (c == ',') {
                letterEnum = Letters_bg.COMMA;
            }

            // Append the mapped character or the original character if no mapping exists
            if (letterEnum != null) {
                result.append(letterEnum.getLetter());  // Append the Unicode representation of the letter
            } else {
                result.append(c);  // Keep non-mapped characters as they are
            }
        }

        return result.toString();
    }
    private String applyColorAndNegativeSpace(String input) {
        StringBuilder result = new StringBuilder();

        // Step 1: Replace hex colors (&#rrggbb → §x§r§r§g§g§b§b)
        Matcher hexMatcher = HEX_COLOR_PATTERN.matcher(input);
        StringBuffer hexBuffer = new StringBuffer();
        while (hexMatcher.find()) {
            String hex = hexMatcher.group(1); // e.g., "ffaa00"
            StringBuilder replacement = new StringBuilder("§x");
            for (char c : hex.toCharArray()) {
                replacement.append('§').append(c);
            }
            hexMatcher.appendReplacement(hexBuffer, Matcher.quoteReplacement(replacement.toString()));
        }
        hexMatcher.appendTail(hexBuffer);
        String coloredInput = hexBuffer.toString();

        // Step 2: Replace legacy codes (&a, &l → §a, §l)
        Matcher legacyMatcher = LEGACY_COLOR_PATTERN.matcher(coloredInput);
        coloredInput = legacyMatcher.replaceAll("§$1");

        // Step 3: Apply character mapping and preserve current formatting
        StringBuilder currentFormat = new StringBuilder(); // Holds active formatting (e.g., §6§l)
        char[] chars = coloredInput.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];

            // Handle § formatting codes
            if (c == '§' && i + 1 < chars.length) {
                currentFormat.append(c).append(chars[i + 1]);
                i++; // Skip formatting code
                continue;
            }

            Letters_bg letterEnum = null;
            if (Character.isLetter(c)) {
                try {
                    letterEnum = Letters_bg.valueOf(String.valueOf(c));
                } catch (IllegalArgumentException ignored) {}
            } else if (Character.isDigit(c)) {
                letterEnum = switch (c) {
                    case '0' -> Letters_bg.ZERO;
                    case '1' -> Letters_bg.ONE;
                    case '2' -> Letters_bg.TWO;
                    case '3' -> Letters_bg.THREE;
                    case '4' -> Letters_bg.FOUR;
                    case '5' -> Letters_bg.FIVE;
                    case '6' -> Letters_bg.SIX;
                    case '7' -> Letters_bg.SEVEN;
                    case '8' -> Letters_bg.EIGHT;
                    case '9' -> Letters_bg.NINE;
                    default -> null;
                };
            } else if (c == ' ') {
                letterEnum = Letters_bg.SPACE;
            } else if (c == ':') {
                letterEnum = Letters_bg.COLON;
            }

            if (letterEnum != null) {
                result.append(currentFormat).append(letterEnum.getLetter()).append(NEGATIVE_SPACE.getLetter());
            } else {
                result.append(currentFormat).append(c).append(NEGATIVE_SPACE.getLetter());
            }
        }

        return result.toString();
    }


}