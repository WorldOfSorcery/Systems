package me.hektortm.woSSystems.unlockables;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.stats.utils.Operation;
import me.hektortm.woSSystems.stats.utils.Stat;
import me.hektortm.woSSystems.unlockables.utils.Action;
import me.hektortm.woSSystems.unlockables.utils.TempUnlockable;
import me.hektortm.woSSystems.unlockables.utils.Unlockable;
import me.hektortm.wosCore.Utils;
import me.hektortm.wosCore.WoSCore;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class UnlockableManager {

    public File unlockableFolder = new File(WoSSystems.getPlugin(WoSSystems.class).getDataFolder(), "unlockables");
    public File unlockableFile = new File(unlockableFolder, "unlockables.yml");
    public File tempUnlockableFile = new File(unlockableFolder, "Unlockables_temp.yml");
    public final Map<String, Unlockable> unlockables = new HashMap<>();
    public final Map<String, TempUnlockable> tempUnlockables = new HashMap<>();

    private final WoSCore core;

    public UnlockableManager(WoSCore core) {
        this.core = core;
    }

    public void createUnlockable(Unlockable unlockable) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(unlockableFile);

        config.set("unlockables.", unlockable);

        try {
            config.save(unlockableFile);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Could not save Unlockable file: " + unlockableFile);


        }
    }

    public void createTempUnlockable(TempUnlockable unlockable) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(tempUnlockableFile);

        config.set("tempunlockables.", unlockable);

        try {
            config.save(tempUnlockableFile);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Could not save TempUnlockable file: " + tempUnlockableFile);
        }
    }

    public void addUnlockable(CommandSender sender, String id) {
        if (unlockables.containsKey(id)) {
            sender.sendMessage("Already exists");
            Utils.error(sender, "unlockables", "error.exists");
            return;
        }

        Unlockable unlockable = new Unlockable(id);
        unlockables.put(id, unlockable);

        createUnlockable(unlockable);
        Utils.successMsg1Value(sender, "unlockables", "create.perm", "%id%", id);
    }

    public void addTempUnlockable(CommandSender sender, String id) {
        if (tempUnlockables.containsKey(id)) {
            Utils.error(sender, "unlockables", "error.exists");
            return;
        }

        TempUnlockable unlockable = new TempUnlockable(id);
        tempUnlockables.put(id, unlockable);

        createTempUnlockable(unlockable);
        Utils.successMsg1Value(sender, "unlockables", "create.perm", "%id%", id);
    }

    public void modifyUnlockable(UUID uuid, String id, Action action) {
        OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
        FileConfiguration playerData = core.getPlayerData(uuid, p.getName());
        File playerFile = new File(core.getDataFolder(), "playerdata" + File.separator + uuid + ".yml");

        if(!unlockables.containsKey(id)) {
            return;
        }

        String path = "unlockables";
        List<String> unlockablesList = playerData.getStringList(path);

        // Modify the list based on the action
        switch (action) {
            case GIVE:
                // Add the unlockable if it's not already in the list
                if (!unlockablesList.contains(id)) {
                    unlockablesList.add(id);
                }
                break;
            case TAKE:
                // Remove the unlockable if it's in the list
                unlockablesList.remove(id);
                break;
        }

        // Save the updated list back to the player file
        playerData.set(path, unlockablesList);
        core.savePlayerData(playerData, playerFile);
    }

    public void modifyTempUnlockable(UUID uuid, String id, Action action) {
        OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
        FileConfiguration playerData = core.getPlayerData(uuid, p.getName());
        File playerFile = new File(core.getDataFolder(), "playerdata" + File.separator + uuid + ".yml");

        if (!tempUnlockables.containsKey(id)) {
            return;
        }

        String path = "tempunlockables";
        List<String> tempUnlockableList = playerData.getStringList(path);

        // Modify the list based on the action
        switch (action) {
            case GIVE:
                // Add the unlockable if it's not already in the list
                if (!tempUnlockableList.contains(id)) {
                    tempUnlockableList.add(id);
                }
                break;
            case TAKE:
                // Remove the unlockable if it's in the list
                tempUnlockableList.remove(id);
                break;
        }

        // Save the updated list back to the player file
        playerData.set(path, tempUnlockableList);
        core.savePlayerData(playerData, playerFile);
    }

    public boolean getPlayerTempUnlockable(OfflinePlayer p, String id) {
        File playerFile = new File(core.getDataFolder(), "playerdata" + File.separator + p.getName() + ".yml");
        FileConfiguration playerData = YamlConfiguration.loadConfiguration(playerFile);

        String path = "tempunlockables";
        List<String> tempUnlockableList = playerData.getStringList(path);
        if (tempUnlockableList.contains(id)) {
            return true;
        }
        return false;
    }

    public List<String> getAllPlayerTempUnlockables(OfflinePlayer p) {
        File playerFile = new File(core.getDataFolder(), "playerdata" + File.separator + p.getName() + ".yml");
        FileConfiguration playerData = YamlConfiguration.loadConfiguration(playerFile);
        String path = "tempunlockables";
        List<String> tempUnlockableList = playerData.getStringList(path);

        return tempUnlockableList;
    }

}
