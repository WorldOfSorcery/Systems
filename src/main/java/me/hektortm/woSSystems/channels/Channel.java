package me.hektortm.woSSystems.channels;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Channel {
    private final String name;
    private final String shortName;
    private final String format;
    private List<String> recipients = new ArrayList<>();
    private final boolean autoJoin;
    private final boolean forceJoin;
    private final int radius;

    public Channel(String name, String shortName, String format, List<String> recipients, boolean autoJoin, boolean forceJoin, int radius) {
        this.name = name;
        this.shortName = shortName;
        this.format = format;
        if (recipients != null) {
            this.recipients.addAll(recipients);
        } else {
            this.recipients = new ArrayList<>();
        }
        this.autoJoin = autoJoin;
        this.forceJoin = forceJoin;
        this.radius = radius;
    }

    public String getName() {
        return name;
    }

    public List<String> getRecipients() {
        return recipients;
    }
    public List<String> addRecipient(String recipient) {
        recipients.add(recipient);
        return recipients;
    }

    public List<String> removeRecipient(String recipient) {
        recipients.remove(recipient);
        return recipients;
    }

    public String getShortName() {
        return shortName;
    }

    public String getFormat() {
        return format;
    }

    public boolean isAutoJoin() {
        return autoJoin;
    }

    public boolean isForceJoin() {
        return forceJoin;
    }

    public int getRadius() {
        return radius;
    }
}
