package me.hektortm.woSSystems.utils;

public enum ConditionType {

    INTERACTION("interaction"),
    GUISLOT("guislot"),
    PARTICLE("particle"),
    HOLOGRAM("hologram"),
    RECIPE("recipe");


    ConditionType(String type) {
        this.type = type;
    }

    private final String type;

    public String getType() {
        return type;
    }


}
