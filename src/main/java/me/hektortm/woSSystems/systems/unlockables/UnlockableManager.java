package me.hektortm.woSSystems.systems.unlockables;

import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.Operations;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class UnlockableManager {

    private final DAOHub hub;

    public UnlockableManager(DAOHub hub) {
        this.hub = hub;
    }

    public void modifyUnlockable(UUID uuid, String id, Operations action) {
        hub.getUnlockableDAO().modifyUnlockable(uuid, id, action);
    }

    public boolean hasPlayerUnlockable(OfflinePlayer p, String id) {
        return hub.getUnlockableDAO().getPlayerUnlockable(p, id);
    }

    public boolean getPlayerTempUnlockable(OfflinePlayer p, String id) {
        return hub.getUnlockableDAO().getPlayerTempUnlockable(p, id);
    }


}
