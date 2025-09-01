package me.hektortm.woSSystems.utils.dataclasses;

public class Constant {

    private final String id;
    private final String value;

    public Constant(String id, String value) {
        this.id = id;
        this.value = value;
    }

    public String getId() {
        return id;
    }
    public String getValue() {
        return value;
    }

}
