package me.hektortm.woSSystems.chat;

import me.hektortm.wosCore.WoSCore;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class NicknameManager {

    private final File currentNickFile;
    private final File reservedNickFile;
    private final ChatManager chatManager;
    private final WoSCore core = WoSCore.getPlugin(WoSCore.class);

    public NicknameManager(ChatManager chatManager) {
        this.chatManager = chatManager;
        currentNickFile = new File(chatManager.chatFolder, "current_nicks");
        reservedNickFile = new File(chatManager.chatFolder, "reserved_nicks");

        if (!currentNickFile.exists()) {
            currentNickFile.mkdir();
        }
        if (!reservedNickFile.exists()) {
            reservedNickFile.mkdir();
        }
    }

    public void saveNickname(Player player, String nickname) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(currentNickFile);
        
        UUID uuid = player.getUniqueId();
        String realName = player.getName();

        // Update nickname and previous nicknames
        config.set(uuid + ".username", realName);
        config.set(uuid + ".nickname", nickname);

        List<String> previousNicks = config.getStringList(uuid + ".previous_nicks");
        previousNicks.add(nickname);
        config.set(uuid + ".previous_nicks", previousNicks);

        saveConfig();
    }

    public void resetNickname(Player player) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(currentNickFile);
        UUID uuid = player.getUniqueId();
        config.set(uuid + ".nickname", null);  // Remove nickname
        saveConfig();
    }

    public String getNickname(Player player) {
        UUID uuid = player.getUniqueId();
        return getNicknamesConfig().getString(uuid + ".nickname", player.getName());
    }

    public String getRealName(Player player) {
        UUID uuid = player.getUniqueId();
        return getNicknamesConfig().getString(uuid + ".username", player.getName());
    }

    private void saveConfig() {
        try {
            getNicknamesConfig().save(currentNickFile);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Could not save nicknames.yml");
        }
    }

    public FileConfiguration getNicknamesConfig() {
        FileConfiguration config = YamlConfiguration.loadConfiguration(currentNickFile);
        return config;
    }
    
}
