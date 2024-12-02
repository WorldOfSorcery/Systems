package me.hektortm.woSSystems.utils.dataclasses;

public class Currency {

    private final String name;
    private final String shortName;
    private final String icon;
    private final String color;

    public Currency(String name, String shortName, String icon, String color){
        this.name = name;
        this.shortName = shortName;
        this.icon = icon;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    public String getIcon() {
        return icon;
    }

    public String getColor() {
        return color;
    }

}
