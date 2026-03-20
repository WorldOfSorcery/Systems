package me.hektortm.woSSystems.systems.guis;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.DyedItemColor;
import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.ActionHandler;
import me.hektortm.woSSystems.utils.ConditionHandler;
import me.hektortm.woSSystems.utils.PlaceholderResolver;
import me.hektortm.woSSystems.utils.dataclasses.Condition;
import me.hektortm.woSSystems.utils.dataclasses.GUI;
import me.hektortm.woSSystems.utils.dataclasses.GUIPage;
import me.hektortm.woSSystems.utils.dataclasses.GUISlot;
import me.hektortm.woSSystems.utils.dataclasses.GUISlotConfig;
import me.hektortm.wosCore.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static io.papermc.paper.datacomponent.item.DyedItemColor.dyedItemColor;
import static me.hektortm.woSSystems.utils.Parsers.hexToBukkitColor;

public class GUIManager implements Listener {

    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final PlaceholderResolver placeholderResolver = plugin.getPlaceholderResolver();
    private final ConditionHandler conditions = plugin.getConditionHandler();
    private final DAOHub hub;
    private final ActionHandler actionHandler;

    /** Tracks which page index each player currently has open. */
    private final Map<UUID, Integer> playerPages = new ConcurrentHashMap<>();

    public GUIManager(DAOHub hub) {
        this.hub = hub;
        actionHandler = new ActionHandler(hub);
    }

    public void openGUI(Player player, String guiId) {
        openGUI(player, guiId, 0);
    }

    public void openGUI(Player player, String guiId, int pageIndex) {
        GUI gui = hub.getGuiDAO().getGUIbyId(guiId);
        if (gui == null) return;

        List<GUIPage> pages = gui.getPages();
        if (pages.isEmpty()) return;

        int clampedPage = Math.max(0, Math.min(pageIndex, pages.size() - 1));
        GUIPage page = pages.get(clampedPage);
        playerPages.put(player.getUniqueId(), clampedPage);

        Inventory inventory = Bukkit.createInventory(
                new GUIHolder(guiId),
                gui.getSize() * 9,
                Utils.parseColorCodeString(gui.getTitle())
        );

        for (GUISlot slot : page.getSlots()) {
            if (!slot.isActive()) continue;
            GUISlotConfig config = resolveConfig(player, slot);
            if (config == null || !config.isVisible()) continue;

            ItemStack item = buildItem(player, config);
            if (item != null) {
                inventory.setItem(slot.getSlot_id(), item);
            }
        }

        if (gui.getOpenActions() != null && !gui.getOpenActions().isEmpty()) {
            actionHandler.executeActions(player, gui.getOpenActions(), ActionHandler.SourceType.GUI, guiId, null);
        }
        player.openInventory(inventory);
    }

    /** Returns the first config whose conditions pass for this player, or null if none match. */
    private GUISlotConfig resolveConfig(Player player, GUISlot slot) {
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

    private ItemStack buildItem(Player player, GUISlotConfig config) {
        String materialName = config.getMaterial() != null
                ? placeholderResolver.resolvePlaceholders(config.getMaterial(), player).toUpperCase()
                : "PAPER";
        Material material = Material.getMaterial(materialName);
        if (material == null) material = Material.PAPER;

        ItemStack item = new ItemStack(material);

        DyedItemColor dyedColor = null;
        if (config.getColor() != null && !config.getColor().isBlank()) {
            dyedColor = dyedItemColor(hexToBukkitColor(config.getColor()));
        }

        if (material == Material.PLAYER_HEAD) {
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            if (meta != null) item.setItemMeta(generateSkullMeta(meta, config, player));
        } else {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) item.setItemMeta(generateItemMeta(meta, config, player));
        }

        if (dyedColor != null) item.setData(DataComponentTypes.DYED_COLOR, dyedColor);

        item.setAmount(config.getAmount());

        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof GUIHolder holder)) return;

        event.setCancelled(true);

        GUI gui = hub.getGuiDAO().getGUIbyId(holder.getGuiId());
        if (gui == null) return;

        int clickedSlot = event.getRawSlot();
        if (clickedSlot < 0 || clickedSlot >= event.getInventory().getSize()) return;

        int pageIndex = playerPages.getOrDefault(player.getUniqueId(), 0);
        List<GUIPage> pages = gui.getPages();
        if (pages.isEmpty() || pageIndex >= pages.size()) return;
        GUIPage page = pages.get(pageIndex);

        for (GUISlot slot : page.getSlots()) {
            if (slot.getSlot_id() != clickedSlot) continue;
            if (!slot.isActive()) break;
            GUISlotConfig config = resolveConfig(player, slot);
            if (config == null || !config.isVisible()) break;
            handleClickActions(player, event.getClick(), config);
            break;
        }
    }

    private void handleClickActions(Player player, ClickType clickType, GUISlotConfig config) {
        List<String> specific = switch (clickType) {
            case LEFT -> config.getLeft_actions();
            case RIGHT -> config.getRight_actions();
            default -> null;
        };

        // Inv check
        if(!plugin.getCitemManager().hasCitemAmount(player, config.getInv_check_id(), config.getInv_check_amount())) {
            player.sendMessage("§cInsufficient ["+hub.getCitemDAO().getCitem(config.getInv_check_id()).getItemMeta().getDisplayName()+"]");
            return;
        }

        // Fire specific left/right actions if present, otherwise fall back to global
        List<String> actions = config.getGlobal_actions();
        if (specific != null) actions.addAll(specific);

        if (actions == null || actions.isEmpty()) return;
        actionHandler.executeActions(player, actions, ActionHandler.SourceType.GUI, config.getGui_id(), null);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof GUIHolder holder)) return;

        playerPages.remove(player.getUniqueId());

        GUI gui = hub.getGuiDAO().getGUIbyId(holder.getGuiId());
        if (gui != null && gui.getCloseActions() != null && !gui.getCloseActions().isEmpty()) {
            actionHandler.executeActions(player, gui.getCloseActions(), ActionHandler.SourceType.GUI, gui.getGuiId(), null);
        }
    }

    private ItemMeta generateItemMeta(ItemMeta meta, GUISlotConfig config, Player player) {
        if (config.getDisplay_name() != null) {
            meta.setDisplayName(Utils.parseColorCodeString(
                    placeholderResolver.resolvePlaceholders(config.getDisplay_name(), player)));
        }

        List<String> lore = parseLore(config.getLore());
        if (!lore.isEmpty()) {
            lore.replaceAll(line -> Utils.parseColorCodeString(placeholderResolver.resolvePlaceholders(line, player)));
            meta.setLore(lore);
        }

        if (config.getModel() != null && !config.getModel().isBlank()) {
            meta.setItemModel(new NamespacedKey("wos",
                    placeholderResolver.resolvePlaceholders(config.getModel(), player)));
        }

        if (config.getTooltip() != null && !config.getTooltip().isEmpty()) {
            if (Objects.equals(config.getTooltip(), "hidden")) {
                meta.setHideTooltip(true);
            }
            else if (config.getTooltip() != null && !config.getTooltip().isEmpty()) {
                NamespacedKey tooltip = new NamespacedKey("minecraft", placeholderResolver.resolvePlaceholders(config.getTooltip(), player));
                meta.setTooltipStyle(tooltip);
            }
        }

        if (config.isEnchanted()) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

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

        return meta;
    }

    private SkullMeta generateSkullMeta(SkullMeta meta, GUISlotConfig config, Player player) {
        if (config.getDisplay_name() != null) {
            meta.setDisplayName(Utils.parseColorCodeString(
                    placeholderResolver.resolvePlaceholders(config.getDisplay_name(), player)));
        }

        List<String> lore = parseLore(config.getLore());
        if (!lore.isEmpty()) {
            lore.replaceAll(line -> Utils.parseColorCodeString(placeholderResolver.resolvePlaceholders(line, player)));
            meta.setLore(lore);
        }

        // For player heads, model holds the Base64 skull texture
        if (config.getModel() != null && !config.getModel().isBlank()) {
            PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
            profile.setProperty(new ProfileProperty("textures", config.getModel()));
            meta.setPlayerProfile(profile);
        }

        if (config.isEnchanted()) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

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

        return meta;
    }

    /** Applies the `mode` field — currently used for tooltip control. */
    private void applyModel(ItemMeta meta, GUISlotConfig config, Player player) {
        String mode = config.getModel();
        if (mode == null || mode.isBlank()) return;
        String resolved = placeholderResolver.resolvePlaceholders(mode, player);
        if ("hidden".equalsIgnoreCase(resolved)) {
            meta.setHideTooltip(true);
        } else {
            meta.setTooltipStyle(new NamespacedKey("minecraft", resolved));
        }
    }

    /** Parses the raw comma-separated lore string stored in the DB into a mutable list. */
    private List<String> parseLore(String raw) {
        if (raw == null || raw.isBlank()) return new java.util.ArrayList<>();
        return Arrays.stream(raw.replace("[", "").replace("]", "").split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(java.util.ArrayList::new));
    }

    private static class GUIHolder implements InventoryHolder {
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
