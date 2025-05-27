package me.hektortm.woSSystems.utils.dataclasses;

import org.bukkit.Location;

import java.util.List;

public class GUI {
    private final String guiId;
    private final int size;
    private final String title;
    private final List<GUISlot> slots;
    private final List<String> open_actions;
    private final List<String> close_actions;

    public GUI(String interactionId, int size, String title, List<GUISlot> slots, List<String> openActions, List<String> closeActions) {
        this.guiId = interactionId;
        this.size = size;
        this.title = title;
        this.slots = slots;
        open_actions = openActions;
        close_actions = closeActions;
    }

    public String getGuiId() {
        return guiId;
    }

    public int getSize() {
        return size;
    }
    public List<String> getOpenActions() {
        return open_actions;
    }
    public List<String> getCloseActions() {
        return close_actions;
    }

    public List<GUISlot> getSlots() {
        return slots;
    }

    public String getTitle() {
        return title;
    }
}
