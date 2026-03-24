package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.SchemaManager;
import me.hektortm.woSSystems.utils.Parsers;
import me.hektortm.woSSystems.utils.model.*;
import me.hektortm.wosCore.database.DatabaseManager;
import me.hektortm.wosCore.database.IDAO;
import me.hektortm.wosCore.discord.DiscordLog;
import me.hektortm.wosCore.discord.DiscordLogger;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Data Access Object for {@link Interaction} and all related child tables
 * ({@code inter_actions}, {@code inter_holograms}, {@code inter_particles},
 * {@code inter_npcs}, {@code inter_blocks}).
 *
 * <p>All interactions are preloaded into an in-memory cache on startup.
 * Subsequent reads are served entirely from cache with no database involvement.
 * Two secondary indexes ({@link #blockIndex} and {@link #npcIndex}) are kept in
 * sync on every bind/unbind so block- and NPC-lookups are also O(1) cache hits.
 */
public class InteractionDAO implements IDAO {

    private final DatabaseManager db;
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final String logName = "InteractionDAO";

    /** Primary cache: interaction id → fully built {@link Interaction}. */
    private final Map<String, Interaction> cache = new ConcurrentHashMap<>();

    /** Secondary index: serialised block location → interaction id. */
    private final Map<String, String> blockIndex = new ConcurrentHashMap<>();

    /** Secondary index: Citizens NPC id → interaction id. */
    private final Map<Integer, String> npcIndex = new ConcurrentHashMap<>();

    public InteractionDAO(DatabaseManager db) { this.db = db; }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    /**
     * Creates or syncs all interaction-related tables and kicks off an async
     * preload of every interaction into the cache.
     *
     * @throws SQLException if table creation or schema sync fails
     */
    @Override
    public void initializeTable() throws SQLException {
        SchemaManager.syncTable(db, Interaction.class);
        SchemaManager.syncTable(db, InteractionAction.class);
        SchemaManager.syncTable(db, InteractionHologram.class);
        SchemaManager.syncTable(db, InteractionParticles.class);
        // inter_npcs and inter_blocks have no dataclass — kept manual
        try (Connection conn = db.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS inter_npcs (
                    npc_id VARCHAR(255) NOT NULL,
                    interaction_id VARCHAR(255) NOT NULL,
                    PRIMARY KEY (npc_id)
                )
            """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS inter_blocks (
                    location TEXT NOT NULL,
                    interaction_id VARCHAR(255) NOT NULL,
                    PRIMARY KEY (location(255))
                )
            """);
        }
        WoSSystems.getInstance().getServer().getScheduler()
                .runTaskAsynchronously(plugin, this::preloadAll);
    }

    /**
     * Re-fetches a single interaction from the database and updates the cache,
     * or evicts it from the cache if it no longer exists.
     *
     * <p>Intended to be called by the web-hook / reload command when a single
     * entry is known to have changed remotely.
     *
     * @param id the interaction id to reload
     * @param p  the player who triggered the reload — receives a title feedback message
     */
    public void reloadFromDB(String id, Player p) {
        String sql = "SELECT id FROM interactions WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Interaction built = buildInteraction(conn, id);
                cache.put(id, built);
                rebuildIndexFor(built);
                p.sendTitle("§aUpdated Interaction", "§e" + id);
            } else {
                Interaction old = cache.remove(id);
                if (old != null) removeIndexFor(old);
                p.sendTitle("§cDeleted Interaction", "§e" + id);
            }
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, logName + ":reload", "Failed to reload interaction from DB: ", e);
        }
    }

    /**
     * Loads every interaction from the database into the cache.
     * Runs asynchronously; each interaction gets its own connection so that
     * iterating the ID result set and building child objects never share a
     * connection that a sub-query might close prematurely.
     *
     * <p>Called automatically from {@link #initializeTable()}.
     */
    public void preloadAll() {
        String sql = "SELECT id FROM interactions";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            int count = 0;
            while (rs.next()) {
                String id = rs.getString("id");
                try (Connection buildConn = db.getConnection()) {
                    Interaction built = buildInteraction(buildConn, id);
                    cache.put(id, built);
                    rebuildIndexFor(built);
                    count++;
                } catch (Exception e) {
                    plugin.getLogger().warning(logName + ": failed to preload '" + id + "': " + e.getMessage());
                }
            }
            plugin.getLogger().info(logName + ": preloaded " + count + " interaction(s) into cache.");
        } catch (SQLException e) {
            WoSSystems.discordLog(Level.SEVERE, logName + ":preload", "Failed to preload interactions into cache: ", e);
        }
    }

    // ── Build chain ──────────────────────────────────────────────────────────

    /**
     * Assembles a complete {@link Interaction} by fetching all child rows
     * (actions, particles, holograms, blocks, NPCs) over the supplied connection.
     *
     * @param conn an open database connection; not closed by this method
     * @param id   the interaction id to build
     * @return the fully populated {@link Interaction}
     * @throws SQLException if any child query fails
     */
    private Interaction buildInteraction(Connection conn, String id) throws SQLException {
        List<InteractionAction> actions = fetchActions(conn, id);
        List<InteractionParticles> particles = fetchParticles(conn, id);
        List<InteractionHologram> holograms = fetchHolograms(conn, id);
        List<Location> blockLocations = fetchBlocks(conn, id);
        List<Integer> npcIds = fetchNPCs(conn, id);
        return new Interaction(id, actions, holograms, particles, blockLocations, npcIds);
    }

    /**
     * Fetches all {@link InteractionAction} rows for a given interaction,
     * ordered by {@code action_id} ascending.
     *
     * @param conn          an open database connection; not closed by this method
     * @param interactionId the interaction id to query
     * @return ordered list of actions, empty if none exist
     * @throws SQLException if the query fails
     */
    private List<InteractionAction> fetchActions(Connection conn, String interactionId) throws SQLException {
        List<InteractionAction> actions = new ArrayList<>();
        String sql = "SELECT * FROM inter_actions WHERE id = ? ORDER BY action_id ASC";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, interactionId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String actionsRaw = rs.getString("actions");
                List<String> parsedActions = parseList(actionsRaw);
                actions.add(new InteractionAction(
                        interactionId,
                        rs.getString("behaviour"),
                        rs.getString("matchtype"),
                        rs.getInt("action_id"),
                        parsedActions
                ));
            }
        }
        return actions;
    }

    /**
     * Fetches all {@link InteractionParticles} rows for a given interaction,
     * ordered by {@code particle_id} ascending.
     *
     * @param conn an open database connection; not closed by this method
     * @param id   the interaction id to query
     * @return ordered list of particle configs, empty if none exist
     * @throws SQLException if the query fails
     */
    private List<InteractionParticles> fetchParticles(Connection conn, String id) throws SQLException {
        List<InteractionParticles> particles = new ArrayList<>();
        String sql = "SELECT * FROM inter_particles WHERE id = ? ORDER BY particle_id ASC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                particles.add(new InteractionParticles(
                        id,
                        rs.getString("behaviour"),
                        rs.getString("matchtype"),
                        rs.getInt("particle_id"),
                        rs.getString("particle"),
                        rs.getString("particle_color")
                ));
            }
        }
        return particles;
    }

    /**
     * Fetches all {@link InteractionHologram} rows for a given interaction.
     *
     * @param conn an open database connection; not closed by this method
     * @param id   the interaction id to query
     * @return list of hologram configs, empty if none exist
     * @throws SQLException if the query fails
     */
    private List<InteractionHologram> fetchHolograms(Connection conn, String id) throws SQLException {
        List<InteractionHologram> holograms = new ArrayList<>();
        String sql = "SELECT * FROM inter_holograms WHERE interaction_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String hologramRaw = rs.getString("hologram");
                holograms.add(new InteractionHologram(
                        id,
                        rs.getInt("hologram_id"),
                        rs.getString("behaviour"),
                        rs.getString("matchtype"),
                        parseList(hologramRaw),
                        rs.getString("settings")
                ));
            }
        }
        return holograms;
    }

    /**
     * Fetches all Citizens NPC ids bound to a given interaction.
     *
     * @param conn an open database connection; not closed by this method
     * @param id   the interaction id to query
     * @return list of NPC ids, empty if none are bound
     * @throws SQLException if the query fails
     */
    private List<Integer> fetchNPCs(Connection conn, String id) throws SQLException {
        List<Integer> npcs = new ArrayList<>();
        String sql = "SELECT npc_id FROM inter_npcs WHERE interaction_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                npcs.add(rs.getInt("npc_id"));
            }
        }
        return npcs;
    }

    /**
     * Fetches all block locations bound to a given interaction.
     *
     * @param conn an open database connection; not closed by this method
     * @param id   the interaction id to query
     * @return list of {@link Location} objects, empty if none are bound
     * @throws SQLException if the query fails
     */
    private List<Location> fetchBlocks(Connection conn, String id) throws SQLException {
        List<Location> blocks = new ArrayList<>();
        String sql = "SELECT location FROM inter_blocks WHERE interaction_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Location loc = Parsers.stringToLocation(rs.getString("location"));
                blocks.add(loc);
            }
        }
        return blocks;
    }

    // ── Index helpers ─────────────────────────────────────────────────────────

    /**
     * Inserts all block locations and NPC ids of the given interaction into
     * the secondary indexes. Called after the interaction is put into the cache.
     *
     * @param inter the interaction whose bindings should be indexed
     */
    private void rebuildIndexFor(Interaction inter) {
        for (Location loc : inter.getBlockLocations()) {
            blockIndex.put(Parsers.locationToString(loc), inter.getInteractionId());
        }
        for (int npcId : inter.getNpcIDs()) {
            npcIndex.put(npcId, inter.getInteractionId());
        }
    }

    /**
     * Removes all block locations and NPC ids of the given interaction from
     * the secondary indexes. Called before the interaction is evicted from the cache.
     *
     * @param inter the interaction whose bindings should be de-indexed
     */
    private void removeIndexFor(Interaction inter) {
        for (Location loc : inter.getBlockLocations()) {
            blockIndex.remove(Parsers.locationToString(loc));
        }
        for (int npcId : inter.getNpcIDs()) {
            npcIndex.remove(npcId);
        }
    }

    // ── Bind / unbind ─────────────────────────────────────────────────────────

    /**
     * Binds a Citizens NPC to an interaction, persisting the link to the database
     * and updating the in-memory cache and {@link #npcIndex} immediately.
     *
     * @param id    the interaction id
     * @param npcId the Citizens NPC id to bind
     * @return {@code true} if the bind was persisted successfully, {@code false} on error
     */
    public boolean bindNPC(String id, int npcId) {
        String sql = "INSERT INTO inter_npcs (npc_id, interaction_id) VALUES (?, ?)";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, npcId);
            pstmt.setString(2, id);
            pstmt.executeUpdate();
            Interaction inter = cache.get(id);
            if (inter != null) {
                inter.getNpcIDs().add(npcId);
                npcIndex.put(npcId, id);
            }
            return true;
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "6e81e051", "Failed to bind Interaction ID(" + id + ") to NPC: ", e
            ));
            return false;
        }
    }

    /**
     * Binds a world block to an interaction, persisting the link to the database
     * and updating the in-memory cache and {@link #blockIndex} immediately.
     *
     * @param id  the interaction id
     * @param loc the block location to bind
     * @return {@code true} if the bind was persisted successfully, {@code false} on error
     */
    public boolean bindBlock(String id, Location loc) {
        String block = Parsers.locationToString(loc);
        String sql = "INSERT INTO inter_blocks (location, interaction_id) VALUES (?, ?)";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, block);
            pstmt.setString(2, id);
            pstmt.executeUpdate();
            Interaction inter = cache.get(id);
            if (inter != null) {
                inter.getBlockLocations().add(loc);
                blockIndex.put(block, id);
            }
            return true;
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "8b80892f", "Failed to bind ID(" + id + ") to block: ", e
            ));
            return false;
        }
    }

    /**
     * Removes the binding between a Citizens NPC and its interaction, deleting
     * the row from the database and updating the cache and {@link #npcIndex}.
     *
     * @param id the Citizens NPC id to unbind
     * @return {@code true} if the row was deleted successfully, {@code false} on error
     */
    public boolean unbindNpc(int id) {
        String sql = "DELETE FROM inter_npcs WHERE npc_id = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            String interId = npcIndex.remove(id);
            if (interId != null) {
                Interaction inter = cache.get(interId);
                if (inter != null) inter.getNpcIDs().remove((Integer) id);
            }
            return true;
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "ca6c88bb", "Failed to unbind Interaction from Npc(" + id + "): ", e
            ));
            return false;
        }
    }

    /**
     * Removes the binding between a block location and its interaction, deleting
     * the row from the database and updating the cache and {@link #blockIndex}.
     *
     * @param loc the block location to unbind
     * @return {@code true} if the row was deleted successfully, {@code false} on error
     */
    public boolean unbindBlock(Location loc) {
        String location = Parsers.locationToString(loc);
        String sql = "DELETE FROM inter_blocks WHERE location = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, location);
            pstmt.executeUpdate();
            String interId = blockIndex.remove(location);
            if (interId != null) {
                Interaction inter = cache.get(interId);
                if (inter != null) inter.getBlockLocations().removeIf(l -> Parsers.locationToString(l).equals(location));
            }
            return true;
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "465cda2f", "Failed to unbind Interaction from block(" + location + "): ", e
            ));
            return false;
        }
    }

    // ── Lookups (cache-only) ──────────────────────────────────────────────────

    /**
     * Returns the cached {@link Interaction} for the given id, or {@code null}
     * if no interaction with that id exists.
     *
     * @param id the interaction id to look up
     * @return the {@link Interaction}, or {@code null}
     */
    public Interaction getInteractionByID(String id) {
        return cache.get(id);
    }

    /**
     * Returns the interaction id bound to the given block location, or
     * {@code null} if no interaction is bound there.
     * Served from {@link #blockIndex} — no database call.
     *
     * @param loc the block location to look up
     * @return the bound interaction id, or {@code null}
     */
    public String getBound(Location loc) {
        return blockIndex.get(Parsers.locationToString(loc));
    }

    /**
     * Returns the interaction id bound to the given Citizens NPC, or
     * {@code null} if no interaction is bound to that NPC.
     * Served from {@link #npcIndex} — no database call.
     *
     * @param npcId the Citizens NPC id to look up
     * @return the bound interaction id, or {@code null}
     */
    public String getNpcBound(int npcId) {
        return npcIndex.get(npcId);
    }

    /**
     * Returns all block locations that have an interaction bound to them.
     * Derived from {@link #blockIndex} — no database call.
     *
     * @return list of bound block locations; may be empty, never {@code null}
     */
    public List<Location> getAllBlockLocations() {
        return blockIndex.keySet().stream()
                .map(Parsers::stringToLocation)
                .collect(Collectors.toList());
    }

    /**
     * Returns {@code true} if an interaction with the given id exists in the cache.
     *
     * @param id the interaction id to check
     * @return {@code true} if the interaction is cached
     */
    public boolean interactionExists(String id) {
        return cache.containsKey(id);
    }

    /**
     * Returns a snapshot of all cached interactions as a new list.
     *
     * @return all cached {@link Interaction} objects; may be empty, never {@code null}
     */
    public List<Interaction> cache() {
        return new ArrayList<>(cache.values());
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    /**
     * Parses a raw bracketed list string (e.g. {@code ["cmd1", "cmd2"]}) into
     * a plain {@link List} of trimmed, unquoted strings.
     *
     * @param raw the raw string to parse; may be {@code null} or blank
     * @return parsed list, empty if the input is null/blank
     */
    private List<String> parseList(String raw) {
        if (raw == null || raw.isBlank()) return new ArrayList<>();
        return Arrays.stream(raw.replace("[", "").replace("]", "").split(","))
                .map(String::trim)
                .map(s -> s.replaceAll("^\"|\"$", ""))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
