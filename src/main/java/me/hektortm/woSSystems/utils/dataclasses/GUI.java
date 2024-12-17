package me.hektortm.woSSystems.utils.dataclasses;

import java.util.Map;

public class GUI {

    private String title;
    private int rows;
    private String openAction;
    private String closeAction;
    private Map<Integer, Slot> slots;

    public GUI(String title, int rows, String openAction, String closeAction, Map<Integer, Slot> slots) {
        this.title = title;
        this.rows = rows;
        this.openAction = openAction;
        this.closeAction = closeAction;
        this.slots = slots;
    }

    // Getters and setters
    public String getTitle() {
        return title;
    }

    public int getRows() {
        return rows;
    }

    public String getOpenAction() {
        return openAction;
    }

    public String getCloseAction() {
        return closeAction;
    }

    public Map<Integer, Slot> getSlots() {
        return slots;
    }

    public Slot getSlot(int slotIndex) {
        return slots.get(slotIndex);
    }

}
