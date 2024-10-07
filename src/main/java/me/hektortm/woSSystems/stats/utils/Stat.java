package me.hektortm.woSSystems.stats.utils;

public class Stat {

    private final String id;
    private final int max;

    public Stat(String id, int max) {
        this.id = id;
        this.max = max;
    }

    public String getId() {
        return id;
    }

    public int getMax() {
        return max;
    }

}
