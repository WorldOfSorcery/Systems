package me.hektortm.woSSystems.systems.guis;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.DyedItemColor;
import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.ActionHandler;
import me.hektortm.woSSystems.utils.ConditionHandler;
import me.hektortm.woSSystems.utils.model.Condition;
import me.hektortm.woSSystems.utils.model.GUI;
import me.hektortm.woSSystems.utils.model.GUIItemBehaviour;
import me.hektortm.woSSystems.utils.model.GUIPage;
import me.hektortm.woSSystems.utils.model.GUISlot;
import me.hektortm.woSSystems.utils.model.GUISlotConfig;
import me.hektortm.wosCore.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static io.papermc.paper.datacomponent.item.DyedItemColor.dyedItemColor;
import static me.hektortm.woSSystems.utils.Parsers.hexToBukkitColor;

/**
 * Opens GUIs and draws them: each page's items (static at their slots, or fluid
 * in order without gaps), and keeps what each player has open so clicks map back
 * to the right item. Clicks themselves are handled by {@link GUIClickHandler}.
 */
public class GUIManager implements Listener {

    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final ConditionHandler conditions = plugin.getConditionHandler();
    private final DAOHub hub;
    private final ActionHandler actionHandler;
    private final GUIClickHandler clicks;

    /**
     * What a player has open: the GUI, the page, which configured slot each
     * inventory slot shows, and the inventory itself (to tell it from a newer one).
     */
    record OpenView(String guiId, int page, Map<Integer, Integer> slots, Inventory inventory) {}

    private final Map<UUID, OpenView> views = new ConcurrentHashMap<>();

    /**
     * Players whose GUI is being replaced by another screen of ours (redraw,
     * another page, the confirm screen): that close isn't a real close.
     */
    private final Set<UUID> switching = ConcurrentHashMap.newKeySet();

    public GUIManager(DAOHub hub) {
        this.hub = hub;
        actionHandler = new ActionHandler(hub);
        clicks = new GUIClickHandler(this, hub, actionHandler);
    }

    // ── Opening ─────────────────────────────────────────────────────────────────

    public void openGUI(Player player, String guiId) {
        openGUI(player, guiId, 0);
    }

    /** Opens a GUI (runs its open actions). */
    public void openGUI(Player player, String guiId, int pageIndex) {
        GUI gui = hub.getGuiDAO().getGUIbyId(guiId);
        if (gui == null || gui.getPages().isEmpty()) return;
        Inventory inventory = draw(player, gui, pageIndex);
        if (gui.getOpenActions() != null && !gui.getOpenActions().isEmpty()) {
            actionHandler.executeActions(player, gui.getOpenActions(), ActionHandler.SourceType.GUI, guiId, null);
        }
        player.openInventory(inventory);
    }

    /** Shows another page of the GUI, or redraws it: no open / close actions. */
    void showPage(Player player, GUI gui, int pageIndex) {
        Inventory inventory = draw(player, gui, pageIndex);
        switchTo(player, () -> player.openInventory(inventory));
    }

    /** Runs {@code open} (which replaces the player's GUI screen) without it counting as a close. */
    void switchTo(Player player, Runnable open) {
        switching.add(player.getUniqueId());
        try {
            open.run();
        } finally {
            switching.remove(player.getUniqueId());
        }
    }

    /** Builds the page's inventory for the player and records it as their open view. */
    private Inventory draw(Player player, GUI gui, int pageIndex) {
        List<GUIPage> pages = gui.getPages();
        int page = Math.max(0, Math.min(pageIndex, pages.size() - 1));
        Inventory inventory = Bukkit.createInventory(new GUIHolder(gui.getGuiId()), gui.getSize() * 9,
                Utils.parseColorCodeString(gui.getTitle()));

        Map<Integer, ItemStack> items = new HashMap<>();
        for (GUISlot slot : pages.get(page).getSlots()) {
            if (!slot.isActive()) continue;
            GUISlotConfig config = resolveConfig(player, slot);
            if (config == null || !config.isVisible()) continue;
            items.put(slot.getSlot_id(), buildItem(config));
        }
        Map<Integer, Integer> layout = GuiRules.layout(new ArrayList<>(items.keySet()), inventory.getSize(), gui.isFluid());
        layout.forEach((at, slot) -> inventory.setItem(at, items.get(slot)));

        views.put(player.getUniqueId(), new OpenView(gui.getGuiId(), page, layout, inventory));
        return inventory;
    }

    @Nullable
    OpenView viewOf(Player player) {
        return views.get(player.getUniqueId());
    }

    /** The first config whose conditions pass for this player, or null if none match. */
    @Nullable
    GUISlotConfig resolveConfig(Player player, GUISlot slot) {
        for (GUISlotConfig config : slot.getConfigs()) {
            List<Condition> conds = config.getConditions();
            if (conds.isEmpty()) return config;
            boolean passes;
            if ("one".equalsIgnoreCase(config.getMatchtype())) {
                passes = conds.stream().anyMatch(c -> conditions.evaluate(player, c, null));
            } else {
                passes = conditions.checkConditions(player, conds, null);
            }
            if (passes) return config;
        }
        return null;
    }

    // ── Events ──────────────────────────────────────────────────────────────────

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof GUIClickHandler.ConfirmHolder confirm) {
            event.setCancelled(true);
            clicks.onConfirmClick(player, confirm, event.getRawSlot());
        } else if (holder instanceof GUIHolder) {
            event.setCancelled(true);
            clicks.onClick(player, event.getInventory(), event.getRawSlot(), event.getClick());
        }
    }

    /** Nothing can be dragged into a GUI or its confirm screen. */
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof GUIHolder || holder instanceof GUIClickHandler.ConfirmHolder) event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        InventoryHolder holder = event.getInventory().getHolder();
        String guiId;
        if (holder instanceof GUIHolder h) guiId = h.getGuiId();
        else if (holder instanceof GUIClickHandler.ConfirmHolder c) guiId = c.guiId();
        else return;
        if (switching.contains(player.getUniqueId())) return;

        // Only forget the view this close is about (a newer GUI may already be open).
        OpenView view = views.get(player.getUniqueId());
        if (view != null && (view.inventory() == event.getInventory() || holder instanceof GUIClickHandler.ConfirmHolder)) {
            views.remove(player.getUniqueId());
        }

        GUI gui = hub.getGuiDAO().getGUIbyId(guiId);
        if (gui != null && gui.getCloseActions() != null && !gui.getCloseActions().isEmpty()) {
            actionHandler.executeActions(player, gui.getCloseActions(), ActionHandler.SourceType.GUI, gui.getGuiId(), null);
        }
    }

    // ── Items ───────────────────────────────────────────────────────────────────

    /** The item a config shows: a custom item or a material (head, colour, lore, cost line …). */
    ItemStack buildItem(GUISlotConfig config) {
        GUIItemBehaviour b = config.getBehaviour();
        ItemStack citem = b.citemId() == null ? null : hub.getCitemDAO().getCitem(b.citemId());
        ItemStack item = citem != null ? citemLook(citem, config) : materialLook(config);
        item.setAmount(Math.max(1, config.getAmount()));
        return item;
    }

    /**
     * A custom item as the look: its model and data. Its name, unless set to use
     * the config's display name; its lore, the config's, or both, as set.
     */
    private ItemStack citemLook(ItemStack item, GUISlotConfig config) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        GUIItemBehaviour b = config.getBehaviour();
        String configName = config.getDisplay_name() == null ? null : Utils.parseColorCodeString(config.getDisplay_name());
        String name = GuiRules.citemName(meta.hasDisplayName() ? meta.getDisplayName() : null, configName, b.citemName());
        if (name != null) meta.setDisplayName(name);

        List<String> configLore = parseLore(config.getLore());
        configLore.replaceAll(Utils::parseColorCodeString);
        List<String> itemLore = meta.getLore() == null ? List.of() : meta.getLore();
        setLore(meta, GuiRules.citemLore(itemLore, configLore, b.citemLore()), b);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack materialLook(GUISlotConfig config) {
        Material material = config.getMaterial() == null ? null : Material.getMaterial(config.getMaterial().toUpperCase());
        if (material == null) {
            plugin.writeLog("GUIManager", Level.WARNING, "GUI " + config.getGui_id() + ": unknown material '" + config.getMaterial() + "', showing PAPER");
            material = Material.PAPER;
        }
        ItemStack item = new ItemStack(material);

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (meta instanceof SkullMeta skull) applySkin(skull, config);
            applyMeta(meta, config);
            item.setItemMeta(meta);
        }

        if (config.getColor() != null && !config.getColor().isBlank()) {
            DyedItemColor dyed = dyedItemColor(hexToBukkitColor(config.getColor()));
            item.setData(DataComponentTypes.DYED_COLOR, dyed);
        }
        return item;
    }

    /** A player head's skin: the head texture (URL or base64), else the legacy base64 in model. */
    private void applySkin(SkullMeta meta, GUISlotConfig config) {
        String texture = config.getBehaviour().headTexture() != null
                ? GuiRules.skinTexture(config.getBehaviour().headTexture())
                : config.getModel();
        if (texture == null || texture.isBlank()) return;
        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
        profile.setProperty(new ProfileProperty("textures", texture));
        meta.setPlayerProfile(profile);
    }

    private void applyMeta(ItemMeta meta, GUISlotConfig config) {
        if (config.getDisplay_name() != null) {
            meta.setDisplayName(Utils.parseColorCodeString(config.getDisplay_name()));
        }
        List<String> lore = parseLore(config.getLore());
        lore.replaceAll(Utils::parseColorCodeString);
        setLore(meta, lore, config.getBehaviour());

        // A player head's model field held its skin before head textures; other items use it as the item model.
        if (!(meta instanceof SkullMeta) && config.getModel() != null && !config.getModel().isBlank()) {
            meta.setItemModel(new NamespacedKey("wos", config.getModel()));
        }
        if (config.getTooltip() != null && !config.getTooltip().isEmpty()) {
            if (Objects.equals(config.getTooltip(), "hidden")) meta.setHideTooltip(true);
            else meta.setTooltipStyle(new NamespacedKey("minecraft", config.getTooltip()));
        }
        if (config.isEnchanted()) meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(
                ItemFlag.HIDE_ATTRIBUTES,
                ItemFlag.HIDE_ENCHANTS,
                ItemFlag.HIDE_UNBREAKABLE,
                ItemFlag.HIDE_ADDITIONAL_TOOLTIP,
                ItemFlag.HIDE_DESTROYS,
                ItemFlag.HIDE_ARMOR_TRIM,
                ItemFlag.HIDE_PLACED_ON,
                ItemFlag.HIDE_STORED_ENCHANTS,
                ItemFlag.HIDE_DYE);
    }

    /** Sets the lore, with the cost / trade price lines the item shows. */
    private void setLore(ItemMeta meta, List<String> lore, GUIItemBehaviour b) {
        String costFormat = Utils.parseColorCodeString(plugin.getLangManager().getMessage("guis", "cost"));
        String priceFormat = Utils.parseColorCodeString(plugin.getLangManager().getMessage("guis", "price"));
        List<String> withPrice = GuiRules.loreWithPrice(lore, b, costFormat, priceFormat);
        meta.setLore(withPrice.isEmpty() ? null : withPrice);
    }

    /** The lore as saved (a JSON array), as a mutable list. */
    private List<String> parseLore(String raw) {
        if (raw == null || raw.isBlank()) return new ArrayList<>();
        // wos-api delivers lore as a JSON array — parse it properly so lines keep
        // their commas and lose the quotes. Fall back to the legacy "[a, b]" split.
        try {
            com.google.gson.JsonElement json = com.google.gson.JsonParser.parseString(raw);
            if (json.isJsonArray()) {
                ArrayList<String> lines = new ArrayList<>();
                for (com.google.gson.JsonElement line : json.getAsJsonArray()) lines.add(line.getAsString());
                return lines;
            }
        } catch (Exception ignored) {
            // not JSON — legacy format below
        }
        return Arrays.stream(raw.replace("[", "").replace("]", "").split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    static class GUIHolder implements InventoryHolder {
        private final String guiId;

        public GUIHolder(String guiId) {
            this.guiId = guiId;
        }

        public String getGuiId() {
            return guiId;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
