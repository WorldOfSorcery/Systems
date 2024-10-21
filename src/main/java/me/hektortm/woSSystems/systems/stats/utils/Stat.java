package me.hektortm.woSSystems.systems.stats.utils;

public class Stat {

    private final String id;
    private final long max;
    private final boolean capped;

    public Stat(String id, long max, boolean capped) {
        this.id = id;
        this.max = max;
        this.capped = capped;
    }

    public String getId() {
        return id;
    }

    public boolean getCapped() {
        return capped;
    }

    public long getMax() {
        return max;
    }

}
