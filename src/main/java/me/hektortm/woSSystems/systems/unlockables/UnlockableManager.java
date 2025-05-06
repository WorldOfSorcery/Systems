package me.hektortm.woSSystems.systems.unlockables;

import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.utils.Operations;
import me.hektortm.woSSystems.utils.dataclasses.TempUnlockable;
import me.hektortm.woSSystems.utils.dataclasses.Unlockable;
import org.bukkit.OfflinePlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UnlockableManager {

    public final Map<String, Unlockable> unlockables = new HashMap<>();
    public final Map<String, TempUnlockable> tempUnlockables = new HashMap<>();
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
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
