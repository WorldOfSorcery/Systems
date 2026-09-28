package me.hektortm.woSSystems.systems.quests;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when a player finishes a quest (successfully or otherwise).
 *
 * <p>Other plugins can listen for this event to hook into the quest completion
 * flow (e.g. grant achievements, update scoreboards).</p>
 */
public class QuestCompleteEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final String questId;
    private final boolean success;

    public QuestCompleteEvent(Player player, String questId, boolean success) {
        this.player  = player;
        this.questId = questId;
        this.success = success;
    }

    public Player  getPlayer()  { return player;  }
    public String  getQuestId() { return questId; }
    public boolean isSuccess()  { return success; }

    @Override
    public @NotNull HandlerList getHandlers() { return HANDLERS; }

    public static HandlerList getHandlerList() { return HANDLERS; }
}
