package me.hektortm.woSSystems.utils;

import me.hektortm.wosCore.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import static me.hektortm.woSSystems.utils.Letters.*;
import static me.hektortm.woSSystems.utils.Letters.STAR;

/**
 * Static and instance utility methods for text conversion, location
 * serialisation, colour parsing, and cooldown formatting.
 *
 * <p>Key capabilities:
 * <ul>
 *   <li>{@link #parseUni(String)} — converts a string to small-caps / stylised
 *       Unicode via the {@link Letters} enum map (instance method).</li>
 *   <li>{@link #parseUniStatic(String)} — alternative Unicode conversion using
 *       a character-array lookup table (static).</li>
 *   <li>{@link #locationToString(Location)} / {@link #stringToLocation(String)} —
 *       serialise and deserialise Bukkit {@link Location} objects to/from a
 *       comma-separated {@code world,x,y,z[,yaw,pitch]} string.</li>
 *   <li>{@link #parseColorFromString(Color)} / {@link #hexToBukkitColor(String)} —
 *       parse ARGB hex and HTML hex strings into Bukkit {@link Color} values.</li>
 *   <li>{@link #formatCooldownTime(long)} — formats a duration in seconds to a
 *       human-readable {@code [D'd ']HH:MM:SS} string.</li>
 * </ul>
 * </p>
 */
public class Parsers {

    private static final String NORMAL_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()_+-=[]{};:'\",.<>/?\\|`~ ";
    private static final String[] STYLIZED_CHARS = {
            "ᴀ", "ʙ", "ᴄ", "ᴅ", "ᴇ", "ꜰ", "ɢ", "ʜ", "ɪ", "ᴊ", "ᴋ", "ʟ", "ᴍ",
            "ɴ", "ᴏ", "ᴘ", "ǫ", "ʀ", "ꜱ", "ᴛ", "ᴜ", "ᴠ", "ᴡ", "x", "ʏ", "ᴢ",
            "ᴀ", "ʙ", "ᴄ", "ᴅ", "ᴇ", "ꜰ", "ɢ", "ʜ", "ɪ", "ᴊ", "ᴋ", "ʟ", "ᴍ",
            "ɴ", "ᴏ", "ᴘ", "ǫ", "ʀ", "ꜱ", "ᴛ", "ᴜ", "ᴠ", "ᴡ", "x", "ʏ", "ᴢ",
            "𝟘", "𝟙", "𝟚", "𝟛", "𝟜", "𝟝", "𝟞", "𝟟", "𝟠", "𝟡", // Stylized numbers
            "!", "@", "#", "$", "%", "^", "&", "*", "(", ")", "_", "+", "-",
            "=", "[", "]", "{", "}", ";", ":", "'", "\"", ",", ".", "<", ">",
            "/", "?", "\\", "|", "`", "~", " " // Symbols and space
    };

    /**
     * Converts a string to stylised Unicode using the {@link Letters} enum.
     *
     * <p>Each character is mapped to its corresponding Unicode small-cap or
     * stylised equivalent.  Characters with no mapping are kept as-is.</p>
     *
     * @param s the input string
     * @return the stylised Unicode string
     */
    public String parseUni(String s) {
        StringBuilder result = new StringBuilder();

        for (char c : s.toCharArray()) {
            Letters letterEnum = null;

            // Map each character to the corresponding enum value
            if (Character.isLetter(c)) {
                letterEnum = Letters.valueOf(String.valueOf(c).toUpperCase());
            } else if (Character.isDigit(c)) {
                switch (c) {
                    case '0': letterEnum = Letters.ZERO; break;
                    case '1': letterEnum = Letters.ONE; break;
                    case '2': letterEnum = Letters.TWO; break;
                    case '3': letterEnum = Letters.THREE; break;
                    case '4': letterEnum = Letters.FOUR; break;
                    case '5': letterEnum = Letters.FIVE; break;
                    case '6': letterEnum = Letters.SIX; break;
                    case '7': letterEnum = Letters.SEVEN; break;
                    case '8': letterEnum = Letters.EIGHT; break;
                    case '9': letterEnum = Letters.NINE; break;
                }
            } else if (c == '_') {
                letterEnum = Letters.UNDERSCORE;
            } else if (c == '-') {
                letterEnum = Letters.DASH;
            } else if (c == '"') {
                letterEnum = QUOTE;
            } else if (c == '&') {
                letterEnum = AMPERSAND;
            } else if (c == '(') {
                letterEnum = BRACKET_OPEN;
            } else if (c == ')') {
                letterEnum = BRACKET_CLOSED;
            } else if (c == ':') {
                letterEnum = COLON;
            } else if (c == '=') {
                letterEnum = EQUALS;
            } else if (c == '!') {
                letterEnum = EXCLAMATION;
            } else if (c == '#') {
                letterEnum = HASHTAG;
            } else if (c == '+') {
                letterEnum = PLUS;
            } else if (c == '?') {
                letterEnum = QUESTION;
            } else if (c == '/') {
                letterEnum = SLASH;
            } else if (c == ';') {
                letterEnum = SEMICOLON;
            } else if (c == '%') {
                letterEnum = PERCENTAGE;
            } else if (c == '.') {
                letterEnum = DOT;
            } else if (c == ',') {
                letterEnum = COMMA;
            } else if (c == '*') {
                letterEnum = STAR;
            }

            // Append the Unicode value or the original character if no mapping exists
            if (letterEnum != null) {
                result.append(letterEnum.getLetter());
            } else {
                result.append(c); // Keep non-mapped characters as is
            }
        }

        return result.toString();
    }


    /**
     * Converts a string to stylised Unicode using a static character-array
     * lookup table.  This is an alternative to {@link #parseUni(String)} that
     * does not require an instance.
     *
     * @param input the input string
     * @return the stylised string, with unmapped characters kept as-is
     */
    public static String parseUniStatic(String input) {
        StringBuilder stylizedText = new StringBuilder();
        for (char c : input.toCharArray()) {
            int index = NORMAL_CHARS.indexOf(c);
            if (index != -1) {
                stylizedText.append(STYLIZED_CHARS[index]);
            } else {
                stylizedText.append(c); // Keep the character as-is if no mapping exists
            }
        }
        return stylizedText.toString();
    }

    /**
     * Serialises a {@link Location} to the format
     * {@code world,x,y,z,yaw,pitch}.
     *
     * @param loc the location to serialise
     * @return a comma-separated string representation
     */
    public static String locationToString(Location loc) {
        return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + "," + loc.getPitch();
    }

    /**
     * Parses a Bukkit {@link Color} from a string in the format
     * {@code Color:[argb0xAARRGGBB]}.
     *
     * <p>Returns {@link Color#WHITE} if the string is {@code null}, empty, or
     * cannot be parsed.</p>
     *
     * @param colorString the ARGB hex string to parse
     * @return the parsed {@link Color}, or {@link Color#WHITE} as fallback
     */
    public static Color parseColorFromString(String colorString) {
        // Expected format: "Color:[argb0xFFFCBA03]"
        if (colorString == null || colorString.isEmpty()) {
            return null; // Default fallback
        }

        try {
            String prefix = "Color:[argb0x";
            String suffix = "]";
            if (colorString.startsWith(prefix) && colorString.endsWith(suffix)) {
                String hex = colorString.substring(prefix.length(), colorString.length() - suffix.length());
                int argb = (int) Long.parseLong(hex, 16); // Must be long for unsigned ints > 0x7FFFFFFF

                int red = (argb >> 16) & 0xFF;
                int green = (argb >> 8) & 0xFF;
                int blue = argb & 0xFF;

                return Color.fromRGB(red, green, blue);
            }
        } catch (Exception e) {
            e.printStackTrace(); // Log the error for debugging
        }
        return Color.WHITE; // Default fallback
    }

    /**
     * Deserialises a location string in the format
     * {@code world,x,y,z[,yaw,pitch]} back to a Bukkit {@link Location}.
     *
     * <p>Yaw and pitch are optional and default to {@code 0.0}.  Returns
     * {@code null} if the string cannot be parsed or the world is not loaded.</p>
     *
     * @param locationString the serialised location string
     * @return the corresponding {@link Location}, or {@code null} on failure
     */
    public static Location stringToLocation(String locationString) {
        try {
            // Split the string by commas
            String[] parts = locationString.split(",");
            if (parts.length < 4) {
                throw new IllegalArgumentException("Location string must have at least 4 parts: world,x,y,z");
            }

            // Extract world name and coordinates
            String worldName = parts[0];
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);

            // Optional: Parse yaw and pitch if present
            float yaw = parts.length > 4 ? Float.parseFloat(parts[4]) : 0.0f;
            float pitch = parts.length > 5 ? Float.parseFloat(parts[5]) : 0.0f;

            // Get the world from the server
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                throw new IllegalArgumentException("World '" + worldName + "' not found");
            }

            // Return the location object
            return new Location(world, x, y, z, yaw, pitch);
        } catch (Exception e) {
            System.err.println("Failed to parse location string: " + locationString);
            e.printStackTrace();
            return null; // Return null if parsing fails
        }
    }


    /**
     * Formats a duration in seconds as a human-readable time string.
     *
     * <p>Output format:
     * <ul>
     *   <li>When days &gt; 0: {@code Dd HH:MM:SS}</li>
     *   <li>When hours &gt; 0: {@code HH:MM:SS}</li>
     *   <li>When minutes &gt; 0: {@code MM:SS}</li>
     *   <li>Otherwise: {@code SS}</li>
     * </ul>
     *
     * @param totalSeconds the total number of seconds remaining
     * @return a formatted time string
     */
    public static String formatCooldownTime(long totalSeconds) {
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (days > 0) {
            return String.format("%dd %02d:%02d:%02d", days, hours, minutes, seconds);
        }
        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
        if (minutes > 0) {
            return String.format("%02d:%02d", minutes, seconds);
        }
        return String.format("%02d", seconds);

    }

    /**
     * Converts a six-digit HTML hex colour string (with or without a leading
     * {@code #}) to a Bukkit {@link Color}.
     *
     * @param hex the hex string, e.g. {@code "#FF8800"} or {@code "FF8800"}
     * @return the corresponding {@link Color}, or {@code null} if the string
     *         is not a valid 6-digit hex value
     */
    public static org.bukkit.Color hexToBukkitColor(String hex) {
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }

        if (hex.length() != 6) {
            return null;
        }

        try {
            int red = Integer.parseInt(hex.substring(0, 2), 16);
            int green = Integer.parseInt(hex.substring(2, 4), 16);
            int blue = Integer.parseInt(hex.substring(4, 6), 16);

            return org.bukkit.Color.fromRGB(red, green, blue);
        } catch (NumberFormatException e) {
            return null;
        }
    }

}
