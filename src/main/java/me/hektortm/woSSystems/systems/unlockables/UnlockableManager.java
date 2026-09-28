package me.hektortm.woSSystems.systems.unlockables;

import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.Operations;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

/**
 * Service layer for player unlockable content.
 *
 * <p>Delegates all persistence to {@link me.hektortm.woSSystems.database.dao.UnlockableDAO}
 * via the {@link DAOHub}.  Unlockables are flags that can be permanently or
 * temporarily granted to players and are checked by the condition system.</p>
 */
public class UnlockableManager {

    private final DAOHub hub;

    /**
     * @param hub the DAO hub used to access unlockable persistence
     */
    public UnlockableManager(DAOHub hub) {
        this.hub = hub;
    }

    /**
     * Grants or revokes a permanent unlockable for a player.
     *
     * @param uuid   the player's UUID
     * @param id     the unlockable definition ID
     * @param action {@link Operations#GIVE} to grant, {@link Operations#TAKE} to revoke
     */
    public void modifyUnlockable(UUID uuid, String id, Operations action) {
        hub.getUnlockableDAO().modifyUnlockable(uuid, id, action);
    }

    /**
     * Returns {@code true} if the player holds the given unlockable as a
     * <em>permanent</em> (non-temp) entry.
     *
     * @param p  the offline player to check
     * @param id the unlockable ID
     * @return {@code true} if the permanent unlockable is held
     */
    public boolean hasPlayerUnlockable(OfflinePlayer p, String id) {
        return hub.getUnlockableDAO().getPlayerUnlockable(p, id);
    }

    /**
     * Returns {@code true} if the player holds the given unlockable as a
     * <em>temporary</em> entry.
     *
     * @param p  the offline player to check
     * @param id the unlockable ID
     * @return {@code true} if the temporary unlockable is held
     */
    public boolean getPlayerTempUnlockable(OfflinePlayer p, String id) {
        return hub.getUnlockableDAO().getPlayerTempUnlockable(p, id);
    }


}
