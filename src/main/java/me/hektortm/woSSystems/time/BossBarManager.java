package me.hektortm.woSSystems.time;

import me.hektortm.woSSystems.utils.Letters;
import me.hektortm.woSSystems.utils.Letters_bg;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static me.hektortm.woSSystems.utils.Letters.*;
import static me.hektortm.woSSystems.utils.Letters.STAR;

public class BossBarManager {

    private final Map<UUID, BossBar> playerBars = new HashMap<>();

    public void createBossBar(Player p) {
        BossBar bossBar = Bukkit.createBossBar("", BarColor.WHITE, BarStyle.SOLID);
        bossBar.setVisible(true);
        bossBar.addPlayer(p);
        playerBars.put(p.getUniqueId(), bossBar);
    }

    public void updateBossBar(Player p, String time, String date, String activityName) {
        BossBar bossBar = playerBars.get(p.getUniqueId());

        if (bossBar == null) {
            // If the player doesn't have a BossBar, we don't proceed
            return;
        }

        time = parseUni(time);
        date = parseUni(date);
        if (activityName != null) {
            activityName = parseUni(activityName);

        }

        // Define the color code for the shadow-free effect (#4e5c24)
        String colorCode = "§x§4§e§5§c§2§4";  // Minecraft color code for #4e5c24

        // Apply the color to the text
        String formattedTime = applyColorAndNegativeSpace(colorCode, time);
        String formattedDate = applyColorAndNegativeSpace(colorCode, date);
        String formattedActivityName = activityName != null ? applyColorAndNegativeSpace(colorCode, activityName) : null;


        // If there is an active activity, use its name as the title; otherwise, use the time and date
        String title = (formattedActivityName != null) ? String.format(
                colorCode+Letters_bg.BORDER_LEFT.getLetter()+Letters_bg.NEGATIVE_SPACE.getLetter() + "%s" + Letters_bg.BORDER_RIGHT.getLetter()
                        + "  " +
                        Letters_bg.BORDER_LEFT.getLetter() + Letters_bg.NEGATIVE_SPACE.getLetter() + "%s" + Letters_bg.BORDER_RIGHT.getLetter()
                        + "  " +
                        Letters_bg.BORDER_LEFT.getLetter() + Letters_bg.NEGATIVE_SPACE.getLetter() + "%s" + Letters_bg.BORDER_RIGHT.getLetter(),
                formattedTime, formattedDate, formattedActivityName) : String.format(
                colorCode+Letters_bg.BORDER_LEFT.getLetter() + Letters_bg.NEGATIVE_SPACE.getLetter() + "%s" + Letters_bg.BORDER_RIGHT.getLetter()
                        + "  " +
                        Letters_bg.BORDER_LEFT.getLetter() + Letters_bg.NEGATIVE_SPACE.getLetter() + "%s" + Letters_bg.BORDER_RIGHT.getLetter(),
                formattedTime, formattedDate);

        bossBar.setTitle(title);
    }



    public void removeBossBar(Player p) {
        BossBar bossBar = playerBars.remove(p.getUniqueId());
        if (bossBar != null) {
            bossBar.removePlayer(p);
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
    private String applyColorAndNegativeSpace(String colorCode, String input) {
        StringBuilder result = new StringBuilder();
        for (char c : input.toCharArray()) {
            // Append the color code
            result.append(colorCode);

            // Map the character to the corresponding enum value if it's a letter
            Letters_bg letterEnum = null;
            if (Character.isLetter(c)) {
                String letter = String.valueOf(c);  // Keep the case as is (no need to convert)
                try {
                    letterEnum = Letters_bg.valueOf(letter);  // Directly map the letter to the enum
                } catch (IllegalArgumentException e) {
                    // Handle the case where no corresponding enum exists (though we assume all letters are mapped)
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
            } else if (c == ' ') {
                letterEnum = Letters_bg.SPACE;
            } else if (c == ':') {
                letterEnum = Letters_bg.COLON;
            }

            // Append the mapped character or the original character if no mapping exists
            if (letterEnum != null) {
                result.append(letterEnum.getLetter());  // Append the Unicode representation of the letter
            } else {
                result.append(c);  // Keep non-mapped characters as they are
            }

            // Add the NEGATIVE_SPACE Unicode after each letter
            result.append(Letters_bg.NEGATIVE_SPACE.getLetter());
        }
        return result.toString();
    }

}
