package me.hektortm.woSSystems.channels;

import java.util.List;

public class Channel {
    private String color;
    private final String name;
    private final String shortName;
    private String format;
    private final List<String> recipients;
    private boolean defaultChannel;
    private boolean autoJoin;
    private boolean forceJoin;
    private boolean hidden;
    private String permission;
    private boolean broadcastable;
    private int radius;

    public Channel(String color, String name, String shortName, String format, List<String> recipients,
                   boolean defaultChannel, boolean autoJoin, boolean forceJoin, boolean hidden,
                   String permission, boolean broadcastable, int radius) {
        this.color = color;
        this.name = name;
        this.shortName = shortName;
        this.format = format;
        this.recipients = recipients;
        this.defaultChannel = defaultChannel;
        this.autoJoin = autoJoin;
        this.forceJoin = forceJoin;
        this.hidden = hidden;
        this.permission = permission;
        this.broadcastable = broadcastable;
        this.radius = radius;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
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

    public boolean isDefaultChannel() {
        return defaultChannel;
    }

    public void setDefaultChannel(boolean defaultChannel) {
        this.defaultChannel = defaultChannel;
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

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public boolean isBroadcastable() {
        return broadcastable;
    }

    public void setBroadcastable(boolean broadcastable) {
        this.broadcastable = broadcastable;
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