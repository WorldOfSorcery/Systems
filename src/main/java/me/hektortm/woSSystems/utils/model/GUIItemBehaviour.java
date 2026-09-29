package me.hektortm.woSSystems.utils.model;

import java.util.List;

/**
 * What a GUI item does besides its commands, as set in the AdminPortal: how it
 * looks (player-head texture or a custom item), its cost, cooldown, extra
 * click types, trade, whether it can be clicked, and what happens after a
 * successful click.
 *
 * @param headTexture      a player head's skin: a texture URL or the base64 value (null: none)
 * @param citemId          show this custom item instead of the material (null: none)
 * @param costCurrency     currency charged on a successful click (null: free)
 * @param costAmount       how much of it (0: free)
 * @param showCost         add the cost to the item's lore
 * @param cooldownId       a cooldown started by a successful click (null: none)
 * @param cooldownActions  run instead of the click while the cooldown runs (empty: the GUI's)
 * @param postUse          "default" (the GUI's), "stay", "close", "next", "previous", "page" or "gui"
 * @param postUseTarget    the page number ("page") or GUI id ("gui")
 */
public record GUIItemBehaviour(
        String headTexture,
        String citemId,
        String costCurrency,
        int costAmount,
        boolean showCost,
        String cooldownId,
        List<String> cooldownActions,
        List<String> shiftRightActions,
        List<String> dropActions,
        Trade trade,
        boolean clickable,
        String postUse,
        String postUseTarget) {

    /** Taken from the player, then given: all at once, only if they have everything taken. */
    public record Trade(List<Entry> take, List<Entry> give) {
        public static final Trade NONE = new Trade(List.of(), List.of());

        public boolean isEmpty() { return take.isEmpty() && give.isEmpty(); }
    }

    /** One item or currency in a trade. {@code type} is "citem" or "currency". */
    public record Entry(String type, String id, int amount) {
        public boolean isCitem() { return "citem".equals(type); }
    }

    /** An item with none of the features: the behaviour of configs saved before them. */
    public static final GUIItemBehaviour PLAIN = new GUIItemBehaviour(null, null, null, 0, false, null,
            List.of(), List.of(), List.of(), Trade.NONE, true, "default", null);

    public boolean hasCost() { return costAmount > 0 && costCurrency != null && !costCurrency.isBlank(); }
}
