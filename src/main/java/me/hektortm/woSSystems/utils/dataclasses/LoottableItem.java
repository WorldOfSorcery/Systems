package me.hektortm.woSSystems.utils.dataclasses;

import me.hektortm.woSSystems.utils.LoottableItemType;

public class LoottableItem {
    private final int weight;
    private final LoottableItemType type;
    private final String value;
    private final int parameter;


    public LoottableItem(int weight, LoottableItemType type, String value, int parameter) {
        this.weight = weight;
        this.type = type;
        this.value = value;
        this.parameter = parameter;
    }

    public int getWeight() {
        return weight;
    }
    public LoottableItemType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public int getParameter() {
        return parameter;
    }
}
