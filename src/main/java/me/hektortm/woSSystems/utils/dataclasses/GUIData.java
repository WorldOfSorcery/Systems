package me.hektortm.woSSystems.utils.dataclasses;


import java.util.List;

public class GUIData {
    private final String id;
    private final String title;
    private final int size;
    private final List<GUIItem> items;

    public GUIData(String id, String title, int size, List<GUIItem> items) {
        this.id = id;
        this.title = title;
        this.size = size;
        this.items = items;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getSize() {
        return size;
    }

    public List<GUIItem> getItems() {
        return items;
    }
}
