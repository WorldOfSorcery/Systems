package me.hektortm.woSSystems.utils.dataclasses;

import me.hektortm.woSSystems.database.annotation.Column;
import me.hektortm.woSSystems.database.annotation.Table;
import org.bukkit.Material;

import java.util.List;

@Table("gui_slots")
public class GUISlot {

    @Column(name = "gui_id", primaryKey = true)
    private final String guiId;

    @Column(name = "page_id", primaryKey = true)
    private final int page_id;

    @Column(name = "slot_id", primaryKey = true)
    private final int slot_id;

    @Column(name = "active")
    private final boolean active;

    private final List<GUISlotConfig> configs;

    public GUISlot(String guiId, int pageId, int slotId, boolean active, List<GUISlotConfig> configs) {
        this.guiId = guiId;
        page_id = pageId;
        slot_id = slotId;
        this.active = active;
        this.configs = configs;
    }

    public String getGuiId() {
        return guiId;
    }

    public int getPage_id() {
        return page_id;
    }

    public int getSlot_id() {
        return slot_id;
    }

    public boolean isActive() {
        return active;
    }

    public List<GUISlotConfig> getConfigs() {
        return configs;
    }
}
