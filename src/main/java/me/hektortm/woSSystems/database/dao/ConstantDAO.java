package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.database.SchemaManager;
import me.hektortm.woSSystems.systems.citems.CitemBuilder;
import me.hektortm.woSSystems.utils.dataclasses.Constant;
import me.hektortm.wosCore.database.DatabaseManager;
import me.hektortm.wosCore.database.IDAO;
import me.hektortm.wosCore.discord.DiscordLog;
import me.hektortm.wosCore.discord.DiscordLogger;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class ConstantDAO implements IDAO {
    private final DatabaseManager db;
    private final DAOHub daoHub;
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final String logName = "ConstantDAO";

    private final Map<String, Constant> cache = new ConcurrentHashMap<>();

    public ConstantDAO(DatabaseManager db, DAOHub daoHub) {
        this.db = db;
        this.daoHub = daoHub;
    }

    @Override
    public void initializeTable() throws SQLException {
        SchemaManager.syncTable(db, Constant.class);

        org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(plugin, this::preloadAll);
    }

    public Constant getConstant(String id) {
        return cache.get(id);
    }

    public void preloadAll() {
        String sql = "SELECT * FROM constants";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            int count = 0;
            while (rs.next()) {
                String id = rs.getString("id");
                try {
                    cache.put(id, new Constant(rs.getString("id"), rs.getString("value")));
                    count++;
                } catch (Exception e) {
                    plugin.getLogger().warning(logName + ": failed to preload '" + id + "': " + e.getMessage());
                }
            }
            plugin.getLogger().info(logName + ": preloaded " + count + " constant(s) into cache.");
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, "COD:preload", "Failed to preload constants into cache: ", e);
        }
    }

    public void reloadFromDB(String id, Player p) {
        String sql = "SELECT id, value FROM constants WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Constant updated = new Constant(rs.getString("id"), rs.getString("value"));
                cache.put(id, updated);
                plugin.getLogger().info(logName + ": reloaded '" + id + "' from DB.");
                p.sendMessage(plugin.getLangManager().getMessage("general", "prefix") + "§aUpdated Constant: §e"+id);
            } else {
                // Deleted on the website → evict
                cache.remove(id);
                plugin.getLogger().info(logName + ": evicted '" + id + "' (not found in DB).");
                p.sendMessage(plugin.getLangManager().getMessage("general", "prefix") + "§cDeleted Constant: §e"+id);
            }
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, "COD:reload", "Failed to reload constant from DB: ", e);
        }
    }
}
