package me.hektortm.woSSystems.utils.dataclasses;

import java.util.List;

public class BoundData {
    private List<String> location; // List of block locations as strings
    private List<String> npc;      // List of NPC IDs

    public BoundData() {}

    public BoundData(List<String> location, List<String> npc) {
        this.location = location;
        this.npc = npc;
    }

    public List<String> getLocation() {
        return location;
    }

    public void setLocation(List<String> location) {
        this.location = location;
    }

    public List<String> getNpc() {
        return npc;
    }

    public void setNpc(List<String> npc) {
        this.npc = npc;
    }
}