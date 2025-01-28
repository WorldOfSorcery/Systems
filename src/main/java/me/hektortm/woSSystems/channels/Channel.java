package me.hektortm.woSSystems.channels;

import java.util.List;

public class Channel {
    private final String name;
    private final String shortName;
    private String format;
    private final List<String> recipients;
    private boolean autoJoin;
    private boolean forceJoin;
    private boolean hidden;
    private int radius;

    public Channel(String name, String shortName, String format, List<String> recipients, boolean autoJoin, boolean forceJoin, boolean hidden, int radius) {
        this.name = name;
        this.shortName = shortName;
        this.format = format;
        this.recipients = recipients;
        this.autoJoin = autoJoin;
        this.forceJoin = forceJoin;
        this.hidden = hidden;
        this.radius = radius;
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    public String getFormat() {
        return format;
    }
    public void setFormat(String format) {
        this.format = format;
    }

    public List<String> getRecipients() {
        return recipients;
    }

    public boolean isAutoJoin() {
        return autoJoin;
    }

    public void setAutoJoin(boolean autoJoin) {
        this.autoJoin = autoJoin;
    }

    public boolean isForceJoin() {
        return forceJoin;
    }

    public void setForceJoin(boolean forceJoin) {
        this.forceJoin = forceJoin;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public void addRecipient(String playerName) {
        if (!recipients.contains(playerName)) {
            recipients.add(playerName);
        }
    }

    public void removeRecipient(String playerName) {
        recipients.remove(playerName);
    }
}