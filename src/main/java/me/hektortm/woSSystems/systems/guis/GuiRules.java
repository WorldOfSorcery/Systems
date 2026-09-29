package me.hektortm.woSSystems.systems.guis;

import me.hektortm.woSSystems.utils.model.GUICheck;
import me.hektortm.woSSystems.utils.model.GUIItemBehaviour;
import me.hektortm.woSSystems.utils.model.GUISlotConfig;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The decisions a GUI click needs, without touching the server: which commands
 * a click runs, where a successful click leads, how a fluid GUI lays its items
 * out, and whether the player meets an item's checks, cost and trade.
 */
public final class GuiRules {

    private GuiRules() {}

    // ── Click types ─────────────────────────────────────────────────────────────

    /**
     * The commands a click runs: its click type's own list if that has any,
     * otherwise the "any click" (global) list. Left, right, shift + right and
     * drop (Q, also with Ctrl) have their own lists; other clicks use the global one.
     */
    public static List<String> actionsFor(ClickType click, GUISlotConfig config) {
        GUIItemBehaviour b = config.getBehaviour();
        List<String> specific = switch (click) {
            case LEFT -> config.getLeft_actions();
            case RIGHT -> config.getRight_actions();
            case SHIFT_RIGHT -> b.shiftRightActions();
            case DROP, CONTROL_DROP -> b.dropActions();
            default -> null;
        };
        if (specific != null && !specific.isEmpty()) return specific;
        List<String> global = config.getGlobal_actions();
        return global == null ? List.of() : global;
    }

    // ── Post-use ────────────────────────────────────────────────────────────────

    /** Where a successful click leads. */
    public sealed interface After permits Redraw, Close, OpenPage, OpenGui {}

    /** Stay: redraw the current page (costs and rules may have changed). */
    public record Redraw() implements After {}

    public record Close() implements After {}

    public record OpenPage(int page) implements After {}

    public record OpenGui(String guiId) implements After {}

    /**
     * The item's post-use, or the GUI's when the item says "default". Next /
     * previous stay on the last / first page; a bad target (no page number, no
     * GUI id) redraws instead of doing something surprising.
     */
    public static After afterClick(String itemPostUse, @Nullable String itemTarget, String guiPostUse, @Nullable String guiTarget,
                                   int currentPage, int pageCount) {
        boolean useGui = itemPostUse == null || itemPostUse.isBlank() || "default".equals(itemPostUse);
        String postUse = useGui ? guiPostUse : itemPostUse;
        String target = useGui ? guiTarget : itemTarget;
        if (postUse == null) return new Redraw();
        return switch (postUse) {
            case "close" -> new Close();
            case "next" -> currentPage + 1 < pageCount ? new OpenPage(currentPage + 1) : new Redraw();
            case "previous" -> currentPage > 0 ? new OpenPage(currentPage - 1) : new Redraw();
            case "page" -> pageNumber(target, pageCount);
            case "gui" -> target == null || target.isBlank() ? new Redraw() : new OpenGui(target);
            default -> new Redraw();
        };
    }

    private static After pageNumber(@Nullable String target, int pageCount) {
        try {
            int page = Integer.parseInt(target == null ? "" : target.trim());
            return page >= 0 && page < pageCount ? new OpenPage(page) : new Redraw();
        } catch (NumberFormatException e) {
            return new Redraw();
        }
    }

    // ── Fluid layout ────────────────────────────────────────────────────────────

    /**
     * Where each shown item goes, as inventory slot → the slot it's configured
     * at. Static: every item at its own slot. Fluid: the shown items, in slot
     * order, fill the inventory from the first slot without gaps (as many as fit).
     */
    public static Map<Integer, Integer> layout(List<Integer> shownSlots, int inventorySize, boolean fluid) {
        Map<Integer, Integer> out = new LinkedHashMap<>();
        List<Integer> sorted = new ArrayList<>(shownSlots);
        sorted.sort(Integer::compare);
        int next = 0;
        for (int slot : sorted) {
            int at = fluid ? next++ : slot;
            if (at >= 0 && at < inventorySize) out.put(at, slot);
        }
        return out;
    }

    // ── Item looks ──────────────────────────────────────────────────────────────

    /**
     * A player head's "textures" value. The portal saves a texture URL
     * (textures.minecraft.net/texture/…), which is wrapped as the game expects;
     * anything else is taken as the base64 value already.
     */
    public static String skinTexture(String headTexture) {
        String t = headTexture.trim();
        if (!t.startsWith("http://") && !t.startsWith("https://")) return t;
        String json = "{\"textures\":{\"SKIN\":{\"url\":\"" + t.replace("\"", "") + "\"}}}";
        return java.util.Base64.getEncoder().encodeToString(json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    /**
     * The lore with the cost appended when the item shows it: a blank line, then
     * {@code costFormat} with %amount% and %currency% filled in.
     */
    public static List<String> loreWithCost(List<String> lore, GUIItemBehaviour b, String costFormat) {
        if (!b.showCost() || !b.hasCost()) return lore;
        List<String> out = new ArrayList<>(lore);
        if (!out.isEmpty()) out.add("");
        out.add(costFormat.replace("%amount%", String.valueOf(b.costAmount())).replace("%currency%", b.costCurrency()));
        return out;
    }

    // ── Requirements ────────────────────────────────────────────────────────────

    /** What the requirements look at: the clicking player's inventory and balances. */
    public interface PlayerState {
        int freeSlots();

        /** How many of this custom item the player carries. */
        int citemCount(String citemId);

        long balance(String currency);
    }

    /** The first requirement not met: kind "inventory", "citem" or "currency", what, and how much is needed. */
    public record Unmet(String kind, @Nullable String id, long needed) {}

    /**
     * The first requirement the player doesn't meet, or null. Checks ("has at
     * least") come first; then what the click would charge: the cost plus
     * everything the trade takes, added up per currency and per item.
     */
    public static @Nullable Unmet firstUnmet(List<GUICheck> checks, GUIItemBehaviour b, PlayerState player) {
        for (GUICheck check : checks) {
            Unmet unmet = switch (check.type()) {
                case INVENTORY -> player.freeSlots() >= check.amount() ? null : new Unmet("inventory", null, check.amount());
                case CITEM -> player.citemCount(check.identifier()) >= check.amount() ? null : new Unmet("citem", check.identifier(), check.amount());
                case CURRENCY -> player.balance(check.identifier()) >= check.amount() ? null : new Unmet("currency", check.identifier(), check.amount());
            };
            if (unmet != null) return unmet;
        }
        for (Map.Entry<String, Long> e : charges(b).entrySet()) {
            String[] key = e.getKey().split(":", 2);
            boolean citem = "citem".equals(key[0]);
            long have = citem ? player.citemCount(key[1]) : player.balance(key[1]);
            if (have < e.getValue()) return new Unmet(key[0], key[1], e.getValue());
        }
        return null;
    }

    /** Everything a click takes, summed: "currency:gold" → 45, "citem:bass" → 3. */
    public static Map<String, Long> charges(GUIItemBehaviour b) {
        Map<String, Long> out = new TreeMap<>();
        if (b.hasCost()) out.merge("currency:" + b.costCurrency(), (long) b.costAmount(), Long::sum);
        for (GUIItemBehaviour.Entry e : b.trade().take()) {
            out.merge((e.isCitem() ? "citem:" : "currency:") + e.id(), (long) e.amount(), Long::sum);
        }
        return out;
    }
}
