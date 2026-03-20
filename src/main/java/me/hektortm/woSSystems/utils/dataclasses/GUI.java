package me.hektortm.woSSystems.utils.dataclasses;

import me.hektortm.woSSystems.database.annotation.Column;
import me.hektortm.woSSystems.database.annotation.Table;

import java.util.List;

@Table("guis")
public class GUI extends BaseEntity {

    @Column(name = "size", notNull = true)
    private final int size;

    @Column(name = "title", type = "TEXT")
    private final String title;

    @Column(name = "type", type = "TEXT")
    private final String type;

    @Column(name = "open_actions", type = "TEXT")
    private final List<String> open_actions;

    @Column(name = "close_actions", type = "TEXT")
    private final List<String> close_actions;

    /** Loaded via join from gui_slots — not a direct DB column. */
    private final List<GUIPage> pages;

    public GUI(String guiId, int size, String title, String type, List<GUIPage> pages, List<String> openActions, List<String> closeActions) {
        super(guiId);
        this.size = size;
        this.title = title;
        this.type = type;
        this.pages = pages;
        open_actions = openActions;
        close_actions = closeActions;
    }

    public String getGuiId()              { return getId();       }
    public int getSize()                  { return size;          }
    public String getTitle()              { return title;         }
    public List<GUIPage> getPages()       { return pages;         }
    public List<String> getOpenActions()  { return open_actions;  }
    public List<String> getCloseActions() { return close_actions; }
}
