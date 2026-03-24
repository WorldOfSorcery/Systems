package me.hektortm.woSSystems.utils.model;

import me.hektortm.woSSystems.database.annotation.Column;
import me.hektortm.woSSystems.database.annotation.Table;

import java.util.List;

/**
 * Mutable definition of a chat channel managed by the channel system.
 *
 * <p>Maps to the {@code channels} database table.  The {@link #recipients}
 * list is populated at runtime from the {@code playerdata_channels} join
 * table and is not a column in the channels table itself.</p>
 *
 * <p>Channels may be radius-limited, hidden, permission-gated, force-joined,
 * or set as the server default.  The {@link #format} string is processed by
 * {@link me.hektortm.woSSystems.systems.channels.ChannelManager} to produce
 * the final chat message.</p>
 */
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

    /**
     * @param color          the channel colour code
     * @param name           the unique channel name (primary key)
     * @param shortName      the abbreviated channel name shown in chat
     * @param format         the message format string
     * @param recipients     the live list of player names currently in this channel
     * @param defaultChannel {@code true} if new players are placed in this channel by default
     * @param autoJoin       {@code true} if players auto-join on login
     * @param forceJoin      {@code true} if players cannot leave this channel
     * @param hidden         {@code true} if the channel is hidden from channel listings
     * @param permission     optional permission node required to join; {@code null} for open channels
     * @param broadcastable  {@code true} if the channel supports server-wide broadcasts
     * @param radius         the chat radius in blocks; {@code 0} for server-wide
     */
    public Channel(String color, String name, String shortName, String format, List<String> recipients,
                   boolean defaultChannel, boolean autoJoin, boolean forceJoin, boolean hidden,
                   String permission, boolean broadcastable, int radius) {
        this.color              = color;
        this.name               = name;
        this.shortName          = shortName;
        this.format             = format;
        this.recipients         = recipients;
        this.defaultChannel     = defaultChannel;
        this.autoJoin           = autoJoin;
        this.forceJoin          = forceJoin;
        this.hidden             = hidden;
        this.permission         = permission;
        this.broadcastable      = broadcastable;
        this.radius             = radius;
    }

    public String getColor()                { return color;           }
    public String getName()                 { return name;            }
    public String getShortName()            { return shortName;       }
    public String getFormat()               { return format;          }
    public List<String> getRecipients()     { return recipients;      }
    public boolean isDefaultChannel()       { return defaultChannel;  }
    public boolean isAutoJoin()             { return autoJoin;        }
    public boolean isForceJoin()            { return forceJoin;       }
    public boolean isHidden()               { return hidden;          }
    public String getPermission()           { return permission;      }
    public boolean isBroadcastable()        { return broadcastable;   }
    public int getRadius()                  { return radius;          }

    /**
     * Adds a player to the channel's recipient list if they are not already present.
     *
     * @param playerName the player's name to add
     */
    public void addRecipient(String playerName) {
        if (!recipients.contains(playerName)) {
            recipients.add(playerName);
        }
    }

    /**
     * Removes a player from the channel's recipient list.
     *
     * @param playerName the player's name to remove
     */
    public void removeRecipient(String playerName) {
        recipients.remove(playerName);
    }
}
