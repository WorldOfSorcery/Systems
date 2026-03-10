package me.hektortm.woSSystems.utils.dataclasses;

import me.hektortm.woSSystems.database.annotation.Column;
import me.hektortm.woSSystems.database.annotation.Table;

import java.util.List;

@Table("guis")
public class GUI extends BaseEntity {

    @Column(notNull = true)
    private final int size;

    @Column
    private final String title;

    @Column(name = "open_actions", type = "TEXT")
    private final List<String> open_actions;

    @Column(name = "close_actions", type = "TEXT")
    private final List<String> close_actions;

    /** Loaded via join from gui_slots — not a direct DB column. */
    private final List<GUISlot> slots;

    public GUI(String guiId, int size, String title, List<GUISlot> slots, List<String> openActions, List<String> closeActions) {
        super(guiId);
        this.size = size;
        this.title = title;
        this.slots = slots;
        open_actions = openActions;
        close_actions = closeActions;
    }

    public String getGuiId()              { return getId();       }
    public int getSize()                  { return size;          }
    public String getTitle()              { return title;         }
    public List<GUISlot> getSlots()       { return slots;         }
    public List<String> getOpenActions()  { return open_actions;  }
    public List<String> getCloseActions() { return close_actions; }
}
