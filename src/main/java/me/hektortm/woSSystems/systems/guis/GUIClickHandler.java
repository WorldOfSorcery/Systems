package me.hektortm.woSSystems.systems.guis;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.ActionHandler;
import me.hektortm.woSSystems.utils.Operations;
import me.hektortm.woSSystems.utils.model.Cooldown;
import me.hektortm.woSSystems.utils.model.GUI;
import me.hektortm.woSSystems.utils.model.GUIItemBehaviour;
import me.hektortm.woSSystems.utils.model.GUIPage;
import me.hektortm.woSSystems.utils.model.GUISlot;
import me.hektortm.woSSystems.utils.model.GUISlotConfig;
import me.hektortm.wosCore.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * What a click on a GUI item does, in order:
 * <ol>
 *   <li>nothing if the item isn't clickable;</li>
 *   <li>while its cooldown runs: its cooldown commands (else the GUI's, else a message) instead;</li>
 *   <li>checks, cost and the trade's "take" must all be met, else a message;</li>
 *   <li>with confirm: a yes / no screen first (and everything is checked again on yes);</li>
 *   <li>charge the cost, do the trade, start the cooldown, play the sound, run the
 *       click type's commands;</li>
 *   <li>then the post-use: redraw, close, another page or another GUI.</li>
 * </ol>
 */
final class GUIClickHandler {

    private static final int CONFIRM_YES = 11, CONFIRM_ITEM = 13, CONFIRM_NO = 15;

    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final GUIManager guis;
    private final DAOHub hub;
    private final ActionHandler actionHandler;

    GUIClickHandler(GUIManager guis, DAOHub hub, ActionHandler actionHandler) {
        this.guis = guis;
        this.hub = hub;
        this.actionHandler = actionHandler;
    }

    /** The confirm screen: which item was clicked, with which click, on which page of which GUI. */
    record ConfirmHolder(String guiId, int page, int slotId, ClickType click) implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            return Bukkit.createInventory(null, 9); // never used: the holder only identifies the screen
        }
    }

    // ── Clicks ──────────────────────────────────────────────────────────────────

    void onClick(Player player, Inventory clicked, int rawSlot, ClickType click) {
        GUIManager.OpenView view = guis.viewOf(player);
        if (view == null || view.inventory() != clicked || rawSlot < 0 || rawSlot >= clicked.getSize()) return;
        Integer slotId = view.slots().get(rawSlot);
        if (slotId == null) return;

        GUI gui = hub.getGuiDAO().getGUIbyId(view.guiId());
        GUISlotConfig config = gui == null ? null : configAt(player, gui, view.page(), slotId);
        if (config == null || !config.getBehaviour().clickable()) return;
        if (blocked(player, gui, config)) return;

        if (config.isConfirm()) openConfirm(player, gui, view.page(), slotId, click, config);
        else succeed(player, gui, view.page(), config, click, clicked);
    }

    void onConfirmClick(Player player, ConfirmHolder confirm, int rawSlot) {
        if (rawSlot != CONFIRM_YES && rawSlot != CONFIRM_NO) return;
        GUI gui = hub.getGuiDAO().getGUIbyId(confirm.guiId());
        if (gui == null) {
            player.closeInventory();
            return;
        }
        GUISlotConfig config = configAt(player, gui, confirm.page(), confirm.slotId());
        // "No", or the item changed / is no longer usable while the screen was open: back to the GUI.
        if (rawSlot == CONFIRM_NO || config == null || !config.getBehaviour().clickable() || blocked(player, gui, config)) {
            guis.showPage(player, gui, confirm.page());
            return;
        }
        succeed(player, gui, confirm.page(), config, confirm.click(), player.getOpenInventory().getTopInventory());
    }

    /** The config the player sees at a slot of a page (its conditions decide which), or null. */
    @Nullable
    private GUISlotConfig configAt(Player player, GUI gui, int page, int slotId) {
        List<GUIPage> pages = gui.getPages();
        if (page < 0 || page >= pages.size()) return null;
        for (GUISlot slot : pages.get(page).getSlots()) {
            if (slot.getSlot_id() != slotId) continue;
            if (!slot.isActive()) return null;
            GUISlotConfig config = guis.resolveConfig(player, slot);
            return config != null && config.isVisible() ? config : null;
        }
        return null;
    }

    // ── Cooldown and requirements ───────────────────────────────────────────────

    /** True (after telling the player) if the item is on cooldown or its requirements aren't met. */
    private boolean blocked(Player player, GUI gui, GUISlotConfig config) {
        GUIItemBehaviour b = config.getBehaviour();
        if (b.cooldownId() != null && hub.getCooldownDAO().isCooldownActive(player, b.cooldownId())) {
            onCooldown(player, gui, config);
            return true;
        }
        GuiRules.Unmet unmet = GuiRules.firstUnmet(config.getChecks(), b, playerState(player));
        if (unmet != null) {
            switch (unmet.kind()) {
                case "inventory" -> Utils.error(player, "guis", "error.inventory", "%amount%", String.valueOf(unmet.needed()));
                case "citem" -> Utils.error(player, "guis", "error.citem", "%amount%", String.valueOf(unmet.needed()), "%id%", unmet.id());
                default -> Utils.error(player, "guis", "error.currency", "%amount%", String.valueOf(unmet.needed()), "%id%", unmet.id());
            }
            return true;
        }
        return false;
    }

    /** The item's cooldown commands, else the GUI's, else a message with the time left. */
    private void onCooldown(Player player, GUI gui, GUISlotConfig config) {
        List<String> actions = config.getBehaviour().cooldownActions();
        if (actions.isEmpty()) actions = gui.getCooldownActions();
        if (actions != null && !actions.isEmpty()) {
            actionHandler.executeActions(player, actions, ActionHandler.SourceType.GUI, gui.getGuiId(), null);
            return;
        }
        Long left = hub.getCooldownDAO().getRemainingSeconds(player, config.getBehaviour().cooldownId());
        Utils.error(player, "guis", "error.cooldown", "%time%", left == null ? "0" : String.valueOf(left));
    }

    private GuiRules.PlayerState playerState(Player player) {
        return new GuiRules.PlayerState() {
            @Override
            public int freeSlots() {
                int free = 0;
                for (ItemStack item : player.getInventory().getStorageContents()) {
                    if (item == null || item.getType() == Material.AIR) free++;
                }
                return free;
            }

            @Override
            public int citemCount(String citemId) {
                return plugin.getCitemManager().countCitem(player, citemId);
            }

            @Override
            public long balance(String currency) {
                return plugin.getEcoManager().getCurrencyBalance(player.getUniqueId(), currency);
            }
        };
    }

    // ── Confirm screen ──────────────────────────────────────────────────────────

    private void openConfirm(Player player, GUI gui, int page, int slotId, ClickType click, GUISlotConfig config) {
        String title = Utils.parseColorCodeString(plugin.getLangManager().getMessage("guis", "confirm.title"));
        Inventory screen = Bukkit.createInventory(new ConfirmHolder(gui.getGuiId(), page, slotId, click), 27, title);
        screen.setItem(CONFIRM_YES, button(Material.LIME_WOOL, "confirm.true"));
        screen.setItem(CONFIRM_ITEM, guis.buildItem(config));
        screen.setItem(CONFIRM_NO, button(Material.RED_WOOL, "confirm.false"));
        guis.switchTo(player, () -> player.openInventory(screen));
    }

    private ItemStack button(Material material, String langKey) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Utils.parseColorCodeString(plugin.getLangManager().getMessage("guis", langKey)));
            item.setItemMeta(meta);
        }
        return item;
    }

    // ── Success ─────────────────────────────────────────────────────────────────

    /**
     * Charges, trades, starts the cooldown, plays the sound, runs the commands,
     * then (next tick) applies the post-use, unless a command already moved the
     * player to another screen ({@code screen} is what they had open when clicking).
     */
    private void succeed(Player player, GUI gui, int page, GUISlotConfig config, ClickType click, Inventory screen) {
        GUIItemBehaviour b = config.getBehaviour();
        charge(player, gui, b);
        give(player, gui, b);
        startCooldown(player, b);

        if (config.getSound() != null && !config.getSound().isBlank()) {
            player.playSound(player.getLocation(), config.getSound(), 1f, 1f);
        }
        List<String> actions = GuiRules.actionsFor(click, config);
        if (!actions.isEmpty()) {
            actionHandler.executeActions(player, actions, ActionHandler.SourceType.GUI, gui.getGuiId(), null);
        }

        GuiRules.After after = GuiRules.afterClick(b.postUse(), b.postUseTarget(), gui.getPostUse(), gui.getPostUseTarget(),
                page, gui.getPages().size());
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (player.getOpenInventory().getTopInventory() != screen) return;
            if (after instanceof GuiRules.Close) player.closeInventory();
            else if (after instanceof GuiRules.OpenPage p) guis.showPage(player, gui, p.page());
            else if (after instanceof GuiRules.OpenGui o) guis.openGUI(player, o.guiId());
            else guis.showPage(player, gui, page); // Redraw
        });
    }

    /** Takes the cost and everything the trade takes (checked just before). */
    private void charge(Player player, GUI gui, GUIItemBehaviour b) {
        for (Map.Entry<String, Long> charge : GuiRules.charges(b).entrySet()) {
            String[] key = charge.getKey().split(":", 2);
            if ("citem".equals(key[0])) {
                plugin.getCitemManager().takeCitem(player, key[1], charge.getValue().intValue());
            } else {
                plugin.getEcoManager().modifyCurrency(player.getUniqueId(), key[1], charge.getValue(), Operations.TAKE, "gui", gui.getGuiId());
            }
        }
    }

    /** Gives what the trade gives. */
    private void give(Player player, GUI gui, GUIItemBehaviour b) {
        for (GUIItemBehaviour.Entry e : b.trade().give()) {
            if (e.isCitem()) {
                plugin.getCitemManager().addCitem(player, e.id(), e.amount());
            } else {
                plugin.getEcoManager().modifyCurrency(player.getUniqueId(), e.id(), e.amount(), Operations.GIVE, "gui", gui.getGuiId());
            }
        }
    }

    /** Starts the item's cooldown and runs the cooldown's start interaction, as the cooldown action does. */
    private void startCooldown(Player player, GUIItemBehaviour b) {
        if (b.cooldownId() == null) return;
        hub.getCooldownDAO().giveCooldown(player, b.cooldownId());
        Cooldown cooldown = hub.getCooldownDAO().getCooldown(b.cooldownId());
        String start = cooldown == null ? null : cooldown.getStart_interaction();
        if (start != null && !start.isBlank()) plugin.getInteractionManager().triggerInteraction(start, player, null);
    }
}
