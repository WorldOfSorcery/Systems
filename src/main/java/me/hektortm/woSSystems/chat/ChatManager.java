package me.hektortm.woSSystems.chat;

import me.hektortm.woSSystems.WoSSystems;

import java.io.File;

public class ChatManager {

    public final File chatFolder;
    private final WoSSystems plugin;

    public ChatManager(WoSSystems plugin) {
        this.plugin = plugin;
        chatFolder = new File(plugin.getDataFolder(), "chat");

        if (!chatFolder.exists()) {
            chatFolder.mkdir();
        }
    }

}
