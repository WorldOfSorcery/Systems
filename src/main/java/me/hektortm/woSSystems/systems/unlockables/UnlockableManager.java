package me.hektortm.woSSystems.systems.unlockables;

import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.systems.unlockables.utils.Action;
import me.hektortm.woSSystems.utils.dataclasses.TempUnlockable;
import me.hektortm.woSSystems.utils.dataclasses.Unlockable;
import me.hektortm.wosCore.logging.LogManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class UnlockableManager {

public final Map<String, Unlockable> unlockables = new HashMap<>();
    public final Map<String, TempUnlockable> tempUnlockables = new HashMap<>();

    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final LogManager logManager = plugin.getLogManager();
    private final DAOHub hub;

    public UnlockableManager(DAOHub hub) {
        this.hub = hub;
    }

    public void deleteUnlockable(String id, boolean isTemp) {
        if (isTemp) {
            try {
                hub.getUnlockableDAO().deleteTempUnlockable(id);
            } catch (SQLException e) {
                plugin.writeLog("UnlockableManager", Level.SEVERE, "Delete Temporary Unlockable error: " + e.getMessage());
            }
        } else {
            try {
                hub.getUnlockableDAO().deleteUnlockable(id);
            } catch (SQLException e) {
                plugin.writeLog("UnlockableManager", Level.SEVERE, "Delete Unlockable error: " + e.getMessage());
            }
        }
    }

    public void createUnlockable(Unlockable unlockable) {
        try {
            hub.getUnlockableDAO().addUnlockable(unlockable.getId());
        } catch (SQLException e) {
            plugin.writeLog("UnlockableManager", Level.SEVERE, "Could not create Unlockable '"+unlockable.getId()+"': " + e.getMessage());
        }
    }

    public void createTempUnlockable(TempUnlockable unlockable) {
        try {
            hub.getUnlockableDAO().addTempUnlockable(unlockable.getId());
        } catch (SQLException e) {
            plugin.writeLog("UnlockableManager", Level.SEVERE, "Could not create Temp Unlockable '"+unlockable.getId()+"': " + e.getMessage());
        }
    }

    public void addUnlockable(CommandSender sender, String id) {
        if (unlockables.containsKey(id)) {
            sender.sendMessage("Already exists");
            return;
        }
        Unlockable unlockable = new Unlockable(id);
        unlockables.put(id, unlockable);
        createUnlockable(unlockable);
        logManager.writeLog((Player) sender, "Unlockable added: " + id);
    }

    public void addTempUnlockable(CommandSender sender, String id) {
        if (tempUnlockables.containsKey(id)) {
            return;
        }
        TempUnlockable unlockable = new TempUnlockable(id);
        tempUnlockables.put(id, unlockable);
        createTempUnlockable(unlockable);
    }

    public void modifyUnlockable(UUID uuid, String id, Action action) {
        try {
            hub.getUnlockableDAO().modifyUnlockable(uuid, id, action);
        } catch (SQLException e) {
            plugin.writeLog("UnlockableManager", Level.SEVERE, "Could not modify Unlockable '"+id+"': " + e.getMessage());
        }
    }

    public void modifyTempUnlockable(UUID uuid, String id, Action action) {
        try {
            hub.getUnlockableDAO().modifyTempUnlockable(uuid, id, action);
        } catch (SQLException e) {
            plugin.writeLog("UnlockableManager", Level.SEVERE, "Could not modify Temp Unlockable '"+id+"': " + e.getMessage());
        }
    }

    public boolean getPlayerUnlockable(OfflinePlayer p, String id) {
        try {
            return hub.getUnlockableDAO().getPlayerUnlockable(p, id);
        } catch (SQLException e) {
            plugin.writeLog("UnlockableManager", Level.SEVERE, "Could not get Unlockable '"+id+"': " + e.getMessage());
            return false;
        }
    }

    public boolean getPlayerTempUnlockable(OfflinePlayer p, String id) {
        try {
            return hub.getUnlockableDAO().getPlayerTempUnlockable(p, id);
        } catch (SQLException e) {
            plugin.writeLog("UnlockableManager", Level.SEVERE, "Could not get Temp Unlockable '"+id+"': " + e.getMessage());
            return false;
        }
    }




}
