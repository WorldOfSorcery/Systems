package me.hektortm.woSSystems.utils;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Splits a {@code send_message} text into plain text and clickable parts. A
 * clickable part is written {@code [text](target)}, the target being
 * {@code interaction:<id>}, {@code action:<command>} or {@code url:<link>};
 * {@code repeat:} in front of it lets it be clicked more than once. Brackets
 * without such a target stay text. Pure: what a click does is up to the caller.
 */
public final class ClickableMessage {

    private static final Pattern LINK =
            Pattern.compile("\\[([^\\[\\]]+)]\\(\\s*(repeat:)?\\s*(interaction|action|url):([^)]*)\\)");

    public enum Kind { INTERACTION, ACTION, URL }

    /** What a click does, and whether it can be clicked again. */
    public record Link(Kind kind, String target, boolean repeat) {}

    /** A piece of the message: its text, and its link if it is clickable. */
    public record Part(String text, @Nullable Link link) {}

    private ClickableMessage() {}

    public static boolean hasLinks(String message) {
        return parse(message).stream().anyMatch(part -> part.link() != null);
    }

    /** The message's pieces in order; one text piece if it has no link. */
    public static List<Part> parse(String message) {
        List<Part> parts = new ArrayList<>();
        Matcher link = LINK.matcher(message);
        int from = 0;
        while (link.find()) {
            String target = link.group(4).trim();
            if (target.isEmpty()) continue; // "[x](action:)" stays text
            if (link.start() > from) parts.add(new Part(message.substring(from, link.start()), null));
            Kind kind = Kind.valueOf(link.group(3).toUpperCase(java.util.Locale.ROOT));
            parts.add(new Part(link.group(1), new Link(kind, target, link.group(2) != null)));
            from = link.end();
        }
        if (from < message.length() || parts.isEmpty()) parts.add(new Part(message.substring(from), null));
        return parts;
    }
}
