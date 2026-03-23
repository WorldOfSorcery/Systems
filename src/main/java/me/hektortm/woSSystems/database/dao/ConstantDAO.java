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

/**
 * DAO for server-wide {@link Constant} key-value pairs stored in the
 * {@code constants} table.
 *
 * <p>All constants are preloaded into an in-memory cache at startup so that
 * reads are zero-latency.  Individual entries can be refreshed or evicted via
 * {@link #reloadFromDB(String, org.bukkit.entity.Player)} when a webhook
 * update is received.</p>
 */
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

    /**
     * Returns the {@link Constant} with the given ID from the in-memory cache,
     * or {@code null} if no such constant exists.
     *
     * @param id the constant ID
     * @return the cached constant, or {@code null}
     */
    public Constant getConstant(String id) {
        return cache.get(id);
    }

    /**
     * Loads all constant definitions from the {@code constants} table into the
     * in-memory cache.  Called asynchronously from {@link #initializeTable()}.
     */
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

    /**
     * Refreshes a single constant in the cache from the database.  If the row
     * no longer exists the entry is evicted from cache.  A title is sent to
     * {@code p} to confirm the result.  Intended for use by webhook update handlers.
     *
     * @param id the constant ID to reload
     * @param p  the player who triggered the reload (receives title feedback)
     */
    public void reloadFromDB(String id, Player p) {
        String sql = "SELECT id, value FROM constants WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Constant updated = new Constant(rs.getString("id"), rs.getString("value"));
                cache.put(id, updated);
                p.sendTitle("§aUpdated Constant", "§e"+id );
            } else {
                cache.remove(id);
                p.sendTitle("§cDeleted Constant", "§e"+id );
            }
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, "COD:reload", "Failed to reload constant from DB: ", e);
        }
    }
}
