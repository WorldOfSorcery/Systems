package me.hektortm.woSSystems.chat;

import me.hektortm.wosCore.WoSCore;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
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
        UUID uuid = player.getUniqueId();
        String realName = player.getName();


    }
}
