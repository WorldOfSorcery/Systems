package me.hektortm.woSSystems.utils.dataclasses;

public class Condition {
    private final String name;       // e.g., "hasItem", "levelMin"
    private final String value;      // e.g., "diamond_sword", "10"
    private final String parameter;  // optional: maybe "slot", "world", etc.

    public Condition(String name, String value, String parameter) {
        this.name = name;
        this.value = value;
        this.parameter = parameter;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getParameter() {
        return parameter;
    }
}

