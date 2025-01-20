package me.hektortm.woSSystems.systems.unlockables;

import me.hektortm.woSSystems.Database.DatabaseManager;
import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.systems.unlockables.utils.Action;
import me.hektortm.woSSystems.utils.dataclasses.TempUnlockable;
import me.hektortm.woSSystems.utils.dataclasses.Unlockable;
import me.hektortm.wosCore.Utils;
import me.hektortm.wosCore.WoSCore;
import me.hektortm.wosCore.logging.LogManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class UnlockableManager {

    public File unlockableFolder = new File(WoSSystems.getPlugin(WoSSystems.class).getDataFolder(), "unlockables");
    public File unlockableFile = new File(WoSSystems.getPlugin(WoSSystems.class).getDataFolder(), "unlockables.yml");
    public File tempUnlockableFile = new File(WoSSystems.getPlugin(WoSSystems.class).getDataFolder(), "Unlockables_temp.yml");
    public final Map<String, Unlockable> unlockables = new HashMap<>();
    public final Map<String, TempUnlockable> tempUnlockables = new HashMap<>();

    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final WoSCore core = plugin.getCore();
    private final LogManager logManager = plugin.getLogManager();
    private final DatabaseManager database;

    public UnlockableManager(DatabaseManager database) {
        this.database = database;
    }

    public void deleteUnlockable(String id, boolean isTemp) {
        if (isTemp) {
            try {
                database.deleteTempUnlockable(id);
            } catch (SQLException e) {
                Bukkit.getLogger().log(Level.SEVERE, "Delete Temp Unlockable error: ", e);
            }
        } else {
            try {
                database.deleteUnlockable(id);
            } catch (SQLException e) {
                Bukkit.getLogger().log(Level.SEVERE, "Delete Unlockable error: ", e);
            }
        }


    }

    public void createUnlockable(Unlockable unlockable) {
        try {
            database.addUnlockable(unlockable.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createTempUnlockable(TempUnlockable unlockable) {
        try {
            database.addTempUnlockable(unlockable.getId());
        } catch (SQLException e) {
            e.printStackTrace();
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
            database.modifyUnlockable(uuid, id, action);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void modifyTempUnlockable(UUID uuid, String id, Action action) {
        try {
            database.modifyTempUnlockable(uuid, id, action);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean getPlayerUnlockable(OfflinePlayer p, String id) {
        try {
            return database.getPlayerUnlockable(p, id);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean getPlayerTempUnlockable(OfflinePlayer p, String id) {
        try {
            return database.getPlayerTempUnlockable(p, id);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }




}
