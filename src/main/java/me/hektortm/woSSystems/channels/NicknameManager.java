package me.hektortm.woSSystems.channels;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
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
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final WoSCore core = WoSCore.getPlugin(WoSCore.class);
    private final LangManager lang = new LangManager(core);
    private final Map<UUID, String> nickRequests = new HashMap<>();
    public final Map<UUID, String> reservedNicks = new HashMap<>();
    private final DAOHub hub = plugin.getDaoHub();
    static Inventory inv;

    public NicknameManager() {

        // Load reserved nicknames and nickname requests from the database
        this.reservedNicks.putAll(hub.getNicknameDAO().getReservedNicknames());
        this.nickRequests.putAll(hub.getNicknameDAO().getNickRequests());
    }

    public void openRequestMenu(Player p) {
        if (nickRequests.isEmpty()) {
            Utils.error(p, "nicknames", "error.no-requests");
            return;
        }

        Inventory menu = Bukkit.createInventory(null, getInvSize(), lang.getMessage("nicknames", "nick.gui.title"));

        nickRequests.forEach((uuid, nick) -> {
            OfflinePlayer requester = Bukkit.getOfflinePlayer(uuid);

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            assert meta != null;

            String warning = "";
            String accept = lang.getMessage("nicknames", "nick.gui.accept");
            String decline = lang.getMessage("nicknames", "nick.gui.decline");

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
                    warning = lang.getMessage("nicknames", "nick.gui.warning")
                            .replace("%player%", Bukkit.getOfflinePlayer(reservedBy).getName());
                    accept = ""; // Clear the accept option
                    decline = lang.getMessage("nicknames", "nick.gui.decline");
                }
            }

            meta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
            meta.setDisplayName(lang.getMessage("nicknames", "nick.gui.name")
                    .replace("%player%", requester.getName()));
            meta.setLore(List.of(
                    lang.getMessage("nicknames", "nick.gui.nick").replace("%nick%", nick),
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
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        hub.getNicknameDAO().saveNickname(uuid, player.getName(), nickname);
    }

    public void resetNickname(UUID uuid) {
        hub.getNicknameDAO().resetNickname(uuid);
    }

    public String getNickname(OfflinePlayer player) {
        return hub.getNicknameDAO().getNickname(player.getUniqueId());
    }

    public void getRealNameOrNickname(CommandSender sender, String input) {
        String result = hub.getNicknameDAO().getRealNameOrNickname(input);
        if (result != null) {
            if (result.equals(input)) {
                Utils.error(sender, "nicknames", "error.realname-invalid");
            } else {
                Utils.successMsg2Values(sender, "nicknames", "realname.success.username", "%result%", result, "%input%", input.replace("_", " "));
            }
        } else {
            Utils.error(sender, "nicknames", "error.realname-invalid");
        }
    }

    public void requestNicknameChange(OfflinePlayer player, String nickname) {
        UUID uuid = player.getUniqueId();
        nickRequests.put(uuid, nickname);
        hub.getNicknameDAO().requestNicknameChange(uuid, nickname);
        Bukkit.getLogger().info("Nickname request submitted for " + player.getName() + ": " + nickname);
    }

    public void approveNicknameChange(UUID uuid) {
        hub.getNicknameDAO().approveNicknameChange(uuid);
        nickRequests.remove(uuid);
        Bukkit.getLogger().info("Nickname approved for " + Bukkit.getOfflinePlayer(uuid).getName());
    }

    public void denyNicknameChange(OfflinePlayer player) {
        UUID uuid = player.getUniqueId();
        hub.getNicknameDAO().denyNicknameChange(uuid);
        nickRequests.remove(uuid);
        Bukkit.getLogger().info("Nickname request denied for " + player.getName());
    }

    public String getPlayersReservedNick(UUID playerUUID) {
        return reservedNicks.get(playerUUID);
    }

    public Map<UUID, String> getReservedNicknames() {
        return Collections.unmodifiableMap(reservedNicks);
    }

    public void reserveNickname(UUID uuid, String nickname) {
        reservedNicks.put(uuid, nickname);
        hub.getNicknameDAO().reserveNickname(uuid, nickname);
    }

    public void unreserveNickname(UUID uuid) {
        reservedNicks.remove(uuid);
        hub.getNicknameDAO().unreserveNickname(uuid);
    }

    public boolean isNicknameReserved(String nickname) {
        return hub.getNicknameDAO().isNicknameReserved(nickname);
    }

    public Map<UUID, String> getNickRequests() {
        return Collections.unmodifiableMap(nickRequests);
    }
}
