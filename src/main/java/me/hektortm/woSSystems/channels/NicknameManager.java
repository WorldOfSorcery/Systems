package me.hektortm.woSSystems.channels;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.wosCore.LangManager;
import me.hektortm.wosCore.Utils;
import me.hektortm.wosCore.WoSCore;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class NicknameManager {

    private final File nickFolder = new File(WoSSystems.getPlugin(WoSSystems.class).getDataFolder(), "channels" + File.separator + "nicknames");
    private final File currentNickFile;
    private final File reservedNickFile;
    private final File nickRequestFile;
    private final WoSCore core = WoSCore.getPlugin(WoSCore.class);
    private final LangManager lang = new LangManager(core);
    private final Map<UUID, String> nickRequests = new HashMap<>();
    public final Map<UUID, String> reservedNicks = new HashMap<>();
    static Inventory inv;

    public NicknameManager() {
        this.currentNickFile = new File(nickFolder, "current_nicks.yml");
        this.reservedNickFile = new File(nickFolder, "reserved_nicks.yml");
        this.nickRequestFile = new File(nickFolder, "nick_requests.yml");

        ensureFileExists(currentNickFile);
        ensureFileExists(reservedNickFile);
        ensureFileExists(nickRequestFile);

        loadReservedNicknames();
        loadNickRequests();
    }

    private void ensureFileExists(File file) {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                Bukkit.getLogger().severe("Could not create file: " + file.getName());
            }
        }
    }

    private void loadReservedNicknames() {
        FileConfiguration config = YamlConfiguration.loadConfiguration(reservedNickFile);
        reservedNicks.clear();

        config.getKeys(false).forEach(uuidString -> {
            try {
                UUID uuid = UUID.fromString(uuidString); // Parse the key as UUID
                String nickname = config.getString(uuidString); // Get the nickname value
                if (nickname != null) {
                    reservedNicks.put(uuid, nickname); // Add to the map
                } else {
                    Bukkit.getLogger().warning("Missing nickname for UUID: " + uuidString);
                }
            } catch (IllegalArgumentException e) {
                Bukkit.getLogger().warning("Invalid UUID format in reserved_nicks.yml: " + uuidString);
            }
        });
    }


    private void loadNickRequests() {
        FileConfiguration config = YamlConfiguration.loadConfiguration(nickRequestFile);
        nickRequests.clear();
        config.getKeys(false).forEach(uuid -> {
            nickRequests.put(UUID.fromString(uuid), config.getString(uuid));
        });
    }

    public void openRequestMenu(Player p) {
        if (nickRequests.isEmpty()) {
            Utils.error(p, "chat", "error.no-requests");
            return;
        }

        Inventory menu = Bukkit.createInventory(null, getInvSize(), lang.getMessage("chat", "nick.gui.title"));

        nickRequests.forEach((uuid, nick) -> {
            OfflinePlayer requester = Bukkit.getOfflinePlayer(uuid);

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            assert meta != null;

            String warning = "";
            String accept = lang.getMessage("chat", "nick.gui.accept");
            String decline = lang.getMessage("chat", "nick.gui.decline");

            // Check if the nickname is reserved
            boolean isReserved = reservedNicks.values().stream()
                    .anyMatch(reservedNick -> reservedNick.equalsIgnoreCase(nick));

            if (isReserved) {
                UUID reservedBy = reservedNicks.entrySet().stream()
                        .filter(entry -> entry.getValue().equalsIgnoreCase(nick))
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .orElse(null);

                // If the nickname is reserved by someone else, show a warning
                if (reservedBy != null && !reservedBy.equals(uuid)) {
                    warning = lang.getMessage("chat", "nick.gui.warning")
                            .replace("%player%", Bukkit.getOfflinePlayer(reservedBy).getName());
                    accept = ""; // Clear the accept option
                    decline = lang.getMessage("chat", "nick.gui.decline");
                }
            }

            meta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
            meta.setDisplayName(lang.getMessage("chat", "nick.gui.name")
                    .replace("%player%", requester.getName()));
            meta.setLore(List.of(
                    lang.getMessage("chat", "nick.gui.nick").replace("%nick%", nick),
                    "§7", warning, accept, decline
            ));
            head.setItemMeta(meta);

            menu.addItem(head);
        });

        p.openInventory(menu);
    }



    private int getInvSize() {
        int size = nickRequests.size();
        if (size <= 9) return 9;
        if (size <= 27) return 27;
        if (size <= 36) return 36;
        return 54;
    }

    public void saveNickname(UUID uuid, String nickname) {
        FileConfiguration config = getNicknamesConfig();

        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        String realName = player.getName();

        config.set(uuid + ".username", realName);
        config.set(uuid + ".nickname", nickname);

        List<String> previousNicks = config.getStringList(uuid + ".previous_nicks");
        if (!previousNicks.contains(nickname)) {
            previousNicks.add(nickname);
        }
        config.set(uuid + ".previous_nicks", previousNicks);

        saveConfig(config);
    }

    public void resetNickname(UUID uuid) {
        FileConfiguration config = getNicknamesConfig();

        config.set(uuid + ".nickname", null);
        saveConfig(config);
    }

    public String getNickname(OfflinePlayer player) {
        UUID uuid = player.getUniqueId();
        return getNicknamesConfig().getString(uuid + ".nickname", player.getName());
    }

    public void getRealNameOrNickname(CommandSender sender, String input) {
        FileConfiguration config = getNicknamesConfig();

        // Check if the input matches a username
        for (String key : config.getKeys(false)) {
            String username = config.getString(key + ".username");
            String currentNickname = config.getString(key + ".nickname");

            // If input matches the username, return the current nickname or username
            if (username != null && username.equalsIgnoreCase(input)) {
                String nickname = currentNickname != null ? currentNickname : username;
                if (nickname != username) {
                    Utils.successMsg2Values(sender, "chat", "realname.success.nickname", "%result%", nickname.replace("_", " "), "%input%", username);
                    return;
                } else {
                    Utils.error(sender, "chat", "error.realname-invalid");
                    return;
                }
            }

            // If input matches the current nickname, return the username
            if (currentNickname != null && currentNickname.equalsIgnoreCase(input)) {
                Utils.successMsg2Values(sender, "chat", "realname.success.username", "%result%", username, "%input%", input.replace("_", " "));
                return;
            }
        }

        return;
    }



    public void requestNicknameChange(OfflinePlayer player, String nickname) {
        UUID uuid = player.getUniqueId();
        nickRequests.put(uuid, nickname);
        saveNickRequests();
        Bukkit.getLogger().info("Nickname request submitted for " + player.getName() + ": " + nickname);
    }

    public void approveNicknameChange(UUID uuid) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        String requestedNickname = nickRequests.remove(uuid);
        if (Objects.equals(requestedNickname, "reset")) {
            resetNickname(uuid);
            return;
        }

        if (requestedNickname != null) {
            saveNickname(uuid, requestedNickname);
            saveNickRequests();
            Bukkit.getLogger().info("Nickname approved for " + player.getName() + ": " + requestedNickname);
        } else {
            Bukkit.getLogger().warning("No nickname request found for " + player.getName());
        }
    }

    public void denyNicknameChange(OfflinePlayer player) {
        UUID uuid = player.getUniqueId();
        if (nickRequests.remove(uuid) != null) {
            saveNickRequests();
            Bukkit.getLogger().info("Nickname request denied for " + player.getName());
        } else {
            Bukkit.getLogger().warning("No nickname request found for " + player.getName());
        }
    }

    private void saveNickRequests() {
        FileConfiguration config = new YamlConfiguration();
        nickRequests.forEach((uuid, nick) -> config.set(uuid.toString(), nick));
        try {
            config.save(nickRequestFile);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Could not save nick_requests.yml");
        }
    }

    private FileConfiguration getNicknamesConfig() {
        return YamlConfiguration.loadConfiguration(currentNickFile);
    }

    private void saveConfig(FileConfiguration config) {
        try {
            config.save(currentNickFile);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Could not save current_nicks.yml");
        }
    }

    public String getPlayersReservedNick(UUID playerUUID) {
        String nick;
        if (reservedNicks.containsKey(playerUUID)) {
            nick = reservedNicks.get(playerUUID);
            return nick;
        }

        return null; // No reserved nickname found
    }


    public Map<UUID, String> getReservedNicknames() {
        return Collections.unmodifiableMap(reservedNicks);
    }

    public void reserveNickname(UUID uuid, String nickname) {
        reservedNicks.put(uuid, nickname);
        saveReservedNicknames();
    }

    public void unreserveNickname(UUID uuid, String nickname) {
        reservedNicks.remove(uuid, nickname);
        saveReservedNicknames();
    }

    private void saveReservedNicknames() {
        FileConfiguration config = new YamlConfiguration();
        reservedNicks.forEach((uuid, nick) -> config.set(uuid.toString(), nick));
        try {
            config.save(reservedNickFile);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Could not save nick_requests.yml");
        }
    }


    public boolean isNicknameReserved(String nickname) {
        return reservedNicks.containsValue(nickname);
    }

    public Map<UUID, String> getNickRequests() {
        return Collections.unmodifiableMap(nickRequests);
    }
}