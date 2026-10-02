package me.hektortm.woSSystems.utils.model;

import me.hektortm.woSSystems.systems.interactions.DisplaySettings;

/** One display entity of an interaction (a block, an item or a custom item shown at every binding). */
public class InteractionDisplay {

    private final String interactionId;
    private final int displayID;
    private final String behaviour; // "break" or "continue"
    private final String matchType; // "all" or "one"
    private final String rawSettings;
    private final DisplaySettings settings;

    public InteractionDisplay(String interactionId, int displayID, String behaviour, String matchType, String rawSettings) {
        this.interactionId = interactionId;
        this.displayID     = displayID;
        this.behaviour     = behaviour;
        this.matchType     = matchType;
        this.rawSettings   = rawSettings == null ? "" : rawSettings;
        this.settings      = DisplaySettings.parse(rawSettings);
    }

    public String getInteractionId()     { return interactionId; }
    public int getDisplayID()            { return displayID;     }
    public String getBehaviour()         { return behaviour;     }
    public String getMatchType()         { return matchType;     }
    /** The settings as stored; a change here means the display must be drawn again. */
    public String getRawSettings()       { return rawSettings;   }
    public DisplaySettings getSettings() { return settings;      }
}
