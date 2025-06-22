package me.hektortm.woSSystems.utils.dataclasses;

public class Cooldown {
    private final String id;
    private final long duration;
    private final String start_interaction;
    private final String end_interaction;

    public Cooldown(String id, long duration, String start_interaction, String end_interaction) {
        this.id = id;
        this.duration = duration;
        this.start_interaction = start_interaction;
        this.end_interaction = end_interaction;
    }

    public String getId() {
        return id;
    }

    public long getDuration() {
        return duration;
    }

    public String getStart_interaction() {
        return start_interaction;
    }
    public String getEnd_interaction() {
        return end_interaction;
    }

}
