package me.hektortm.woSSystems.utils.dataclasses;

import me.hektortm.woSSystems.database.annotation.Column;
import me.hektortm.woSSystems.database.annotation.Table;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@Table("gui_slot_configs")
public class GUISlotConfig {

    @Column(name = "gui_id", primaryKey = true)
    private final String gui_id;

    @Column(name = "page_id", primaryKey = true)
    private final int page_id;

    @Column(name = "slot_id", primaryKey = true)
    private final int slot_id;

    @Column(name = "config_id", primaryKey = true)
    private final String config_id;

    @Column(name = "matchtype", type = "TEXT")
    private final String matchtype;

    @Column(name = "amount", type = "INTEGER", defaultValue = "1")
    private final int amount;

    @Column(name = "visible", type = "BOOLEAN")
    private final boolean visible;

    @Column(name = "material", type = "TEXT")
    private final String material;

    @Column(name = "display_name", type = "TEXT")
    private final String display_name;

    @Column(name = "lore", type = "TEXT")
    private final String lore;

    @Column(name = "model", type = "TEXT")
    private final String model;

    @Column(name = "color", type = "TEXT")
    private final String color;

    @Column(name = "tooltip", type = "TEXT")
    private final String tooltip;

    @Column(name = "enchanted")
    private final boolean enchanted;

    /** Built at runtime from the raw DB fields above — not a DB column itself. */
    private final ItemStack guiItem;

    @Column(name = "global_actions", type = "TEXT")
    private final List<String> global_actions;

    @Column(name = "right_actions", type = "TEXT")
    private final List<String> right_actions;

    @Column(name = "left_actions", type = "TEXT")
    private final List<String> left_actions;

    @Column(name = "confirm")
    private final boolean confirm;

    @Column(name = "sound", type = "TEXT")
    private final String sound;

    @Column(name = "inv_check_id", type = "TEXT")
    private final String inv_check_id;

    @Column(name = "inv_check_amount", type = "INTEGER")
    private final int inv_check_amount;

    private final List<Condition> conditions;

    public GUISlotConfig(String guiId, int pageId, int slotId, String configId, String matchtype, int amount,
                         boolean visible, String material, String displayName, String lore,
                         String model, String color, String tooltip, boolean enchanted, ItemStack guiItem,
                         List<String> globalActions, List<String> rightActions, List<String> leftActions,
                         boolean confirm, String sound, String invCheckId, int invCheckAmount,
                         List<Condition> conditions) {
        gui_id = guiId;
        page_id = pageId;
        slot_id = slotId;
        config_id = configId;
        this.matchtype = matchtype;
        this.amount = amount;
        this.visible = visible;
        this.material = material;
        display_name = displayName;
        this.lore = lore;
        this.model = model;
        this.color = color;
        this.tooltip = tooltip;
        this.enchanted = enchanted;
        this.guiItem = guiItem;
        global_actions = globalActions;
        right_actions = rightActions;
        left_actions = leftActions;
        this.confirm = confirm;
        this.sound = sound;
        inv_check_id = invCheckId;
        inv_check_amount = invCheckAmount;
        this.conditions = conditions;
    }

    public String getGui_id()               { return gui_id;           }
    public int getPage_id()                 { return page_id;          }
    public int getSlot_id()                 { return slot_id;          }
    public String getConfig_id()            { return config_id;        }
    public String getMatchtype()            { return matchtype;        }
    public boolean isVisible()              { return visible;          }
    public int getAmount()                  { return amount;           }
    public String getMaterial()             { return material;         }
    public String getDisplay_name()         { return display_name;     }
    public String getLore()                 { return lore;             }
    public String getModel()                { return model;            }
    public String getColor()                { return color;            }
    public boolean isEnchanted()            { return enchanted;        }
    public ItemStack getGuiItem()           { return guiItem;          }
    public List<String> getGlobal_actions() { return global_actions;   }
    public List<String> getRight_actions()  { return right_actions;    }
    public List<String> getLeft_actions()   { return left_actions;     }
    public boolean isConfirm()              { return confirm;          }
    public String getTooltip()              { return tooltip;          }
    public String getSound()                { return sound;            }
    public String getInv_check_id()         { return inv_check_id;     }
    public int getInv_check_amount()     { return inv_check_amount; }
    public List<Condition> getConditions()  { return conditions;       }
}
