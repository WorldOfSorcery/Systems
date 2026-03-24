package me.hektortm.woSSystems.utils.model;

import me.hektortm.woSSystems.database.annotation.Column;
import me.hektortm.woSSystems.database.annotation.Table;

@Table("activities")
public class Activity extends BaseEntity {

    @Column(name = "isEnabled", defaultValue = "FALSE")
    private final boolean isEnabled;

    @Column
    private final String name;

    @Column
    private final String message;

    @Column(name = "isDefault", defaultValue = "FALSE")
    private final boolean isDefault;

    @Column
    private final String date;

    @Column(name = "start_time", defaultValue = "0")
    private final int startTime;

    @Column(name = "end_time", defaultValue = "0")
    private final int endTime;

    @Column(name = "start_interaction")
    private final String startInteraction;

    @Column(name = "end_interaction")
    private final String endInteraction;

    public Activity(String id, boolean isEnabled, String name, String message, boolean isDefault,
                    String date, int startTime, int endTime, String startInteraction, String endInteraction) {
        super(id);
        this.isEnabled        = isEnabled;
        this.name             = name;
        this.message          = message;
        this.isDefault        = isDefault;
        this.date             = date;
        this.startTime        = startTime;
        this.endTime          = endTime;
        this.startInteraction = startInteraction;
        this.endInteraction   = endInteraction;
    }

    public boolean getIsEnabled()        { return isEnabled;        }
    public String  getName()             { return name;             }
    public String  getMessage()          { return message;          }
    public boolean isDefault()           { return isDefault;        }
    public String  getDate()             { return date;             }
    public int     getStartTime()        { return startTime;        }
    public int     getEndTime()          { return endTime;          }
    public String  getStartInteraction() { return startInteraction; }
    public String  getEndInteraction()   { return endInteraction;   }
}
