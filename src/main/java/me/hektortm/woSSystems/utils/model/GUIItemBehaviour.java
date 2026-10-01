package me.hektortm.woSSystems.utils.model;

import java.util.List;

/**
 * What a GUI item does besides its commands, as set in the AdminPortal: how it
 * looks (player-head texture or a custom item), its cost, cooldown, extra
 * click types, trade, whether it can be clicked, and what happens after a
 * successful click.
 *
 * @param headTexture      a player head's skin: a texture URL, the base64 value or a player name; may hold placeholders like {player_name} (null: none)
 * @param citemId          show this custom item instead of the material (null: none)
 * @param citemName        with a custom item: keep its name (else the config's display name)
 * @param citemLore        with a custom item: "citem" (its lore), "config" (the config's) or "both" (its, then the config's)
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
        boolean citemName,
        String citemLore,
        String costCurrency,
        int costAmount,
        boolean showCost,
        String cooldownId,
        List<String> cooldownActions,
        List<String> shiftLeftActions,
        List<String> shiftRightActions,
        List<String> dropActions,
        Trade trade,
        boolean clickable,
        String postUse,
        String postUseTarget) {

    /**
     * Taken from the player, then given: all at once, only if they have everything
     * taken. {@code show}: the price (what's taken) is shown in the item's lore;
     * {@code showGive}: what the player gets is shown there too.
     */
    public record Trade(List<Entry> take, List<Entry> give, boolean show, boolean showGive) {
        public static final Trade NONE = new Trade(List.of(), List.of(), false);

        /** A trade that doesn't show what it gives. */
        public Trade(List<Entry> take, List<Entry> give, boolean show) {
            this(take, give, show, false);
        }

        public boolean isEmpty() { return take.isEmpty() && give.isEmpty(); }
    }

    /** One item or currency in a trade. {@code type} is "citem" or "currency". */
    public record Entry(String type, String id, int amount) {
        public boolean isCitem() { return "citem".equals(type); }
    }

    /** An item with none of the features: the behaviour of configs saved before them. */
    public static final GUIItemBehaviour PLAIN = new GUIItemBehaviour(null, null, true, "citem", null, 0, false, null,
            List.of(), List.of(), List.of(), List.of(), Trade.NONE, true, "default", null);

    public boolean hasCost() { return costAmount > 0 && costCurrency != null && !costCurrency.isBlank(); }
}
