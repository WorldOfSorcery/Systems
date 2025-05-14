package me.hektortm.woSSystems.utils;

public enum ConditionType {

    INTERACTION("interaction"),
    PARTICLE("particle"),
    RECIPE("recipe");


    ConditionType(String type) {
        this.type = type;
    }

    private final String type;

    public String getType() {
        return type;
    }


}
