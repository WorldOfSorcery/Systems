package me.hektortm.woSSystems.utils.dataclasses;

import me.hektortm.woSSystems.database.annotation.Column;
import me.hektortm.woSSystems.database.annotation.Table;
import org.bukkit.Material;

import java.util.List;

@Table("gui_slots")
public class GUISlot {

    @Column(name = "gui_id", primaryKey = true)
    private final String guiId;

    @Column(primaryKey = true)
    private final int slot;

    @Column(name = "slot_id")
    private final int slot_id;

    @Column(name = "matchtype")
    private final String matchType;

    @Column(name = "material", type = "VARCHAR(255)")
    private final Material material;

    @Column(name = "display_name")
    private final String displayName;

    @Column(type = "TEXT")
    private final List<String> lore;

    @Column(type = "TEXT")
    private final String model;

    @Column(type = "VARCHAR(7)")
    private final String color;

    @Column(notNull = true)
    private final int amount;

    @Column
    private final String tooltip;

    @Column
    private final boolean enchanted;

    @Column(name = "right_click", type = "TEXT")
    private final List<String> right_actions;

    @Column(name = "left_click", type = "TEXT")
    private final List<String> left_actions;

    @Column(notNull = true, defaultValue = "false")
    private final boolean visible;

    public GUISlot(String guiId, int slot, int slot_id, String matchType, Material material, String displayName, List<String> lore, String model, String color, int amount, String tooltip, boolean enchanted, List<String> rightActions, List<String> leftActions, boolean visible) {
        this.guiId = guiId;
        this.slot = slot;
        this.slot_id = slot_id;
        this.matchType = matchType;
        this.material = material;
        this.displayName = displayName;
        this.lore = lore;
        this.model = model;
        this.color = color;
        this.amount = amount;
        this.tooltip = tooltip;
        this.enchanted = enchanted;
        right_actions = rightActions;
        left_actions = leftActions;
        this.visible = visible;
    }

    public String getGuiId()              { return guiId;        }
    public int getSlot()                  { return slot;         }
    public int getSlotId()                { return slot_id;      }
    public String getMatchType()          { return matchType;    }
    public Material getMaterial()         { return material;     }
    public String getDisplayName()        { return displayName;  }
    public List<String> getLore()         { return lore;         }
    public String getModel()              { return model;        }
    public String getColor()              { return color;        }
    public int getAmount()                { return amount;       }
    public String getTooltip()            { return tooltip;      }
    public boolean isEnchanted()          { return enchanted;    }
    public List<String> getRight_actions(){ return right_actions;}
    public List<String> getLeft_actions() { return left_actions; }
    public boolean isVisible()            { return visible;      }
}
