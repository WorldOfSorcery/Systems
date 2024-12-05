package me.hektortm.woSSystems.utils.dataclasses;

public class ParticleData {
    private String type;  // The type of particle (e.g., "villager_happy")
    private String color; // Optional particle color (e.g., "green")

    public ParticleData() {}

    public ParticleData(String type, String color) {
        this.type = type;
        this.color = color;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}