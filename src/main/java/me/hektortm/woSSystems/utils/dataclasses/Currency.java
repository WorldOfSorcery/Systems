package me.hektortm.woSSystems.utils.dataclasses;

public class Currency {

    private final String name;
    private final String shortName;
    private final String icon;
    private final String color;
    private final boolean hiddenIfZero;

    public Currency(String name, String shortName, String icon, String color, boolean hiddenIfZero){
        this.name = name;
        this.shortName = shortName;
        this.icon = icon;
        this.color = color;
        this.hiddenIfZero = hiddenIfZero;
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

    public boolean isHiddenIfZero() {
        return hiddenIfZero;
    }
}
