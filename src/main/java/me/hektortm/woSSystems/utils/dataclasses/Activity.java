package me.hektortm.woSSystems.utils.dataclasses;

public class Activity
{
    private final String id;
    private final String name;
    private final String message;
    private final boolean isDefault;
    private final String date;
    private final int startTime;
    private final int endTime;
    private final String startInteraction;
    private final String endInteraction;

    public Activity(String id, String name, String message, boolean isDefault, String date, int startTime, int endTime, String startInteraction, String endInteraction) {
        this.id = id;
        this.name = name;
        this.message = message;
        this.isDefault = isDefault;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.startInteraction = startInteraction;
        this.endInteraction = endInteraction;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getMessage() {
        return message;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public String getDate() {
        return date;
    }

    public int getStartTime() {
        return startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public String getStartInteraction() {
        return startInteraction;
    }

    public String getEndInteraction() {
        return endInteraction;
    }
}
