package me.hektortm.woSSystems.channels;

import me.hektortm.woSSystems.database.annotation.Column;
import me.hektortm.woSSystems.database.annotation.Table;

import java.util.List;

@Table("channels")
public class Channel {

    @Column(name = "name", primaryKey = true, notNull = true)
    private final String name;

    @Column(name = "short_name", notNull = true)
    private final String shortName;

    @Column(notNull = true)
    private String color;

    @Column(notNull = true)
    private String format;

    @Column(name = "default_channel", type = "TINYINT(1)", notNull = true)
    private boolean defaultChannel;

    @Column(name = "autojoin", type = "TINYINT(1)", notNull = true)
    private boolean autoJoin;

    @Column(name = "forcejoin", type = "TINYINT(1)", notNull = true)
    private boolean forceJoin;

    @Column(type = "TINYINT(1)", notNull = true)
    private boolean hidden;

    @Column(type = "TINYINT(1)", notNull = true)
    private boolean broadcastable;

    @Column
    private String permission;

    @Column(notNull = true)
    private int radius;

    /** Managed via playerdata_channels — not a column in the channels table. */
    private final List<String> recipients;

    public Channel(String color, String name, String shortName, String format, List<String> recipients,
                   boolean defaultChannel, boolean autoJoin, boolean forceJoin, boolean hidden,
                   String permission, boolean broadcastable, int radius) {
        this.color         = color;
        this.name          = name;
        this.shortName     = shortName;
        this.format        = format;
        this.recipients    = recipients;
        this.defaultChannel = defaultChannel;
        this.autoJoin      = autoJoin;
        this.forceJoin     = forceJoin;
        this.hidden        = hidden;
        this.permission    = permission;
        this.broadcastable = broadcastable;
        this.radius        = radius;
    }

    public String getColor()          { return color;         }
    public void setColor(String color){ this.color = color;   }

    public String getName()           { return name;          }
    public String getShortName()      { return shortName;     }

    public String getFormat()             { return format;             }
    public void setFormat(String format)  { this.format = format;      }

    public List<String> getRecipients()   { return recipients;          }

    public boolean isDefaultChannel()                        { return defaultChannel;         }
    public void setDefaultChannel(boolean defaultChannel)    { this.defaultChannel = defaultChannel; }

    public boolean isAutoJoin()              { return autoJoin;          }
    public void setAutoJoin(boolean autoJoin){ this.autoJoin = autoJoin; }

    public boolean isForceJoin()               { return forceJoin;           }
    public void setForceJoin(boolean forceJoin){ this.forceJoin = forceJoin;  }

    public boolean isHidden()              { return hidden;           }
    public void setHidden(boolean hidden)  { this.hidden = hidden;    }

    public String getPermission()                   { return permission;          }
    public void setPermission(String permission)    { this.permission = permission;}

    public boolean isBroadcastable()                     { return broadcastable;          }
    public void setBroadcastable(boolean broadcastable)  { this.broadcastable = broadcastable; }

    public int getRadius()             { return radius;          }
    public void setRadius(int radius)  { this.radius = radius;   }

    public void addRecipient(String playerName) {
        if (!recipients.contains(playerName)) {
            recipients.add(playerName);
        }
    }

    public void removeRecipient(String playerName) {
        recipients.remove(playerName);
    }
}
