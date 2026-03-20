package me.hektortm.woSSystems.utils.dataclasses;

import me.hektortm.woSSystems.database.annotation.Column;
import me.hektortm.woSSystems.database.annotation.Table;

import java.util.List;

@Table("gui_pages")
public class GUIPage {

    @Column(name = "gui_id", type = "TEXT")
    private final String gui_id;

    @Column(name = "page_id", type = "INT")
    private final int page_id;

    private final List<GUISlot> slots;

    public GUIPage(String guiId, int pageId, List<GUISlot> slots) {
        gui_id = guiId;
        page_id = pageId;
        this.slots = slots;
    }

    public String getGuiId()        {    return gui_id;     }
    public int getPageId()          {    return page_id;    }
    public List<GUISlot> getSlots() {    return slots;      }

}
