package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.database.SchemaManager;
import me.hektortm.woSSystems.utils.Parsers;
import me.hektortm.woSSystems.utils.dataclasses.Interaction;
import me.hektortm.woSSystems.utils.dataclasses.InteractionAction;
import me.hektortm.woSSystems.utils.dataclasses.InteractionHologram;
import me.hektortm.woSSystems.utils.dataclasses.InteractionParticles;
import me.hektortm.wosCore.Utils;
import me.hektortm.wosCore.database.DatabaseManager;
import me.hektortm.wosCore.database.IDAO;
import me.hektortm.wosCore.discord.DiscordLog;
import me.hektortm.wosCore.discord.DiscordLogger;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class InteractionDAO implements IDAO {

    private final DatabaseManager db;
    private final DAOHub hub;
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final String logName = "InteractionDAO";

    public InteractionDAO(DatabaseManager db, DAOHub hub) {
        this.db = db;
        this.hub = hub;
    }

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
                    PRIMARY KEY (interaction_id)
                )
            """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS inter_blocks (
                    location TEXT NOT NULL,
                    interaction_id VARCHAR(255) NOT NULL,
                    PRIMARY KEY (interaction_id)
                )
            """);
        }
    }

    public List<InteractionAction> getActionsForInteraction(String interactionId) {
        List<InteractionAction> actions = new ArrayList<>();

        String sql = "SELECT * FROM inter_actions WHERE id = ? ORDER BY action_id ASC";

        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, interactionId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String description = rs.getString("description");
                String behaviour = rs.getString("behaviour");
                String matchType = rs.getString("matchtype");
                int actionId = rs.getInt("action_id");
                String actionsRaw = rs.getString("actions");

                // Assuming actions are stored like ["cmd1", "cmd2"]
                List<String> parsedActions = Arrays.stream(actionsRaw.replace("[", "").replace("]", "").split(","))
                        .map(String::trim)
                        .map(s -> s.replaceAll("^\"|\"$", "")) // remove surrounding quotes
                        .collect(Collectors.toList());

                actions.add(new InteractionAction(interactionId, description, behaviour, matchType, actionId, parsedActions));
            }

        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "6cb8760c", "Failed to get Actions for Interaction ID("+interactionId+"): ", e
            ));
        }

        return actions;
    }

    public List<InteractionParticles> getParticlesForInteraction(String id) {
        List<InteractionParticles> particles = new ArrayList<>();

        String sql = "SELECT * FROM inter_particles WHERE id = ? ORDER BY particle_id ASC";

        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String description = rs.getString("description");
                String behaviour = rs.getString("behaviour");
                String matchType = rs.getString("matchtype");
                int particleId = rs.getInt("particle_id");
                String particle = rs.getString("particle");
                String particleColor = rs.getString("particle_color");

                particles.add(new InteractionParticles(id, description, behaviour, matchType, particleId, particle, particleColor));
            }
            return particles;
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "5b32a7e5", "Failed to get Particles for Interaction ID("+id+"): ", e
            ));
        }
        return null;
    }

    public List<InteractionHologram> getHologramsForInteraction(String id) {
        List<InteractionHologram> holograms = new ArrayList<>();

        String sql = "SELECT * FROM inter_holograms WHERE interaction_id = ?";

        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int hologramId = rs.getInt("hologram_id");
                String description = rs.getString("description");
                String behaviour = rs.getString("behaviour");
                String matchType = rs.getString("matchtype");
                String hologramRaw = rs.getString("hologram");

                // Assuming holograms are stored like ["line1", "line2"]
                List<String> parsedHologram = Arrays.stream(hologramRaw.replace("[", "").replace("]", "").split(","))
                        .map(String::trim)
                        .map(s -> s.replaceAll("^\"|\"$", "")) // remove surrounding quotes
                        .collect(Collectors.toList());

                String settings = rs.getString("settings");

                holograms.add(new InteractionHologram(id, hologramId, description, behaviour, matchType, parsedHologram, settings));
            }

            return holograms;
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Failed to get holograms: " + e);
        }
        return null;
    }


    public boolean bindNPC(String id, int npcId) {
        String sql = "INSERT INTO inter_npcs (npc_id, interaction_id) VALUES (?, ?)";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, npcId);
            pstmt.setString(2, id);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "6e81e051", "Failed to bind Interaction ID("+id+") to NPC: ", e
            ));
            return false;
        }
    }

    public List<Integer> getNPCs(String id) {
        List<Integer> npcs = new ArrayList<>();
        String sql = "SELECT npc_id FROM inter_npcs WHERE interaction_id = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                npcs.add(rs.getInt("npc_id"));
            }
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "9efbf58a", "Failed to get NPCs for Interaction ID("+id+"): ", e
            ));
        }
        return npcs;
    }

    public String getNPCInteraction(int id) {
        String sql = "SELECT interaction_id FROM inter_npcs WHERE npc_id = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("interaction_id");
            }
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "8b80892f", "Failed to get NPC Interaction ID("+id+"): ", e
            ));
            return null;
        }
        return null;
    }

    public boolean bindBlock(String id, Location loc) {
        String block = Parsers.locationToString(loc);
        String sql = "INSERT INTO inter_blocks (location, interaction_id) VALUES (?, ?)";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, block);
            pstmt.setString(2, id);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "8b80892f", "Failed to bind ID("+id+") to block: ", e
            ));
            return false;
        }

    }

    public List<Location> getBlocks(String id) {
        List<Location> blocks = new ArrayList<>();
        String sql = "SELECT location FROM inter_blocks WHERE interaction_id = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String locStr = rs.getString("location");
                Location loc = Parsers.stringToLocation(locStr);
                blocks.add(loc);
            }
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "90441c51", "Failed to get Block Locations for ID("+id+"): ", e
            ));
        }
        return blocks;
    }

    public List<Location> getAllBlockLocations() {
        String sql = "SELECT * FROM inter_blocks";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            List<Location> locations = new ArrayList<>();
            while (rs.next()) {
                String locStr = rs.getString("location");
                Location loc = Parsers.stringToLocation(locStr);
                locations.add(loc);
            }
            return locations;
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "364d5b8e", "Failed to get all Block Locations: ", e
            ));
        }
        return List.of();
    }

    public String getInterOnBlock(Location loc) {
        String location = Parsers.locationToString(loc);
        String sql = "SELECT interaction_id FROM inter_blocks WHERE location = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, location);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("interaction_id");
            }
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "364d5b8e", "Failed to get Interactions for Block("+location+"): ", e
            ));
            return null;
        }
        return null;
    }


    public Interaction getInteractionByID(String id) {
        String sql = "SELECT * FROM interactions WHERE id = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String interactionId = rs.getString("id");
                List<InteractionAction> actions = getActionsForInteraction(interactionId);
                List<InteractionHologram> holograms = getHologramsForInteraction(interactionId);
                List<InteractionParticles> particles = getParticlesForInteraction(interactionId);
                List<Location> blockLocations = getBlocks(interactionId);
                List<Integer> npcIDs = getNPCs(interactionId);
                return new Interaction(interactionId, actions, holograms, particles, blockLocations, npcIDs);
            }
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "d993e0c9", "Failed to get Interactions for ID("+id+"): ", e
            ));
        }
        return null;
    }

    public List<Interaction> getInteractions() {
        List<String> ids = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT id FROM interactions")) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) ids.add(rs.getString("id"));
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(Level.SEVERE, plugin, "c3956d21", "Failed to get Interaction IDs: ", e));
            return new ArrayList<>();
        }

        if (ids.isEmpty()) return new ArrayList<>();

        // Bulk-load all child rows — one query per table
        Map<String, List<InteractionAction>> actionsMap = new HashMap<>();
        Map<String, List<InteractionHologram>> hologramsMap = new HashMap<>();
        Map<String, List<InteractionParticles>> particlesMap = new HashMap<>();
        Map<String, List<Location>> blocksMap = new HashMap<>();
        Map<String, List<Integer>> npcsMap = new HashMap<>();

        try (Connection conn = db.getConnection()) {
            // inter_actions
            try (PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM inter_actions ORDER BY id, action_id ASC")) {
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    String id = rs.getString("id");
                    String actionsRaw = rs.getString("actions");
                    List<String> parsedActions = Arrays.stream(actionsRaw.replace("[", "").replace("]", "").split(","))
                            .map(String::trim).map(s -> s.replaceAll("^\"|\"$", "")).collect(Collectors.toList());
                    actionsMap.computeIfAbsent(id, k -> new ArrayList<>())
                            .add(new InteractionAction(id, rs.getString("description"), rs.getString("behaviour"), rs.getString("matchtype"), rs.getInt("action_id"), parsedActions));
                }
            }

            // inter_holograms
            try (PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM inter_holograms")) {
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    String id = rs.getString("interaction_id");
                    String hologramRaw = rs.getString("hologram");
                    List<String> parsedHologram = Arrays.stream(hologramRaw.replace("[", "").replace("]", "").split(","))
                            .map(String::trim).map(s -> s.replaceAll("^\"|\"$", "")).collect(Collectors.toList());
                    hologramsMap.computeIfAbsent(id, k -> new ArrayList<>())
                            .add(new InteractionHologram(id, rs.getInt("hologram_id"), rs.getString("description"), rs.getString("behaviour"), rs.getString("matchtype"), parsedHologram, rs.getString("settings")));
                }
            }

            // inter_particles
            try (PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM inter_particles ORDER BY id, particle_id ASC")) {
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    String id = rs.getString("id");
                    particlesMap.computeIfAbsent(id, k -> new ArrayList<>())
                            .add(new InteractionParticles(id, rs.getString("description"), rs.getString("behaviour"), rs.getString("matchtype"), rs.getInt("particle_id"), rs.getString("particle"), rs.getString("particle_color")));
                }
            }

            // inter_blocks
            try (PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM inter_blocks")) {
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    String id = rs.getString("interaction_id");
                    Location loc = Parsers.stringToLocation(rs.getString("location"));
                    blocksMap.computeIfAbsent(id, k -> new ArrayList<>()).add(loc);
                }
            }

            // inter_npcs
            try (PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM inter_npcs")) {
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    String id = rs.getString("interaction_id");
                    npcsMap.computeIfAbsent(id, k -> new ArrayList<>()).add(rs.getInt("npc_id"));
                }
            }
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(Level.SEVERE, plugin, "c3956d21b", "Failed to bulk-load Interaction children: ", e));
            return new ArrayList<>();
        }

        // Assemble Interaction objects from the maps
        List<Interaction> interactions = new ArrayList<>();
        for (String id : ids) {
            interactions.add(new Interaction(
                    id,
                    actionsMap.getOrDefault(id, new ArrayList<>()),
                    hologramsMap.getOrDefault(id, new ArrayList<>()),
                    particlesMap.getOrDefault(id, new ArrayList<>()),
                    blocksMap.getOrDefault(id, new ArrayList<>()),
                    npcsMap.getOrDefault(id, new ArrayList<>())
            ));
        }
        return interactions;
    }

    public boolean unbindNpc(int id) {
        String sql = "DELETE FROM inter_npcs WHERE npc_id = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "ca6c88bb", "Failed to unbind Interaction from Npc("+id+"): ", e
            ));
            return false;
        }
    }

    public boolean unbindBlock(Location loc) {
        String sql = "DELETE FROM inter_blocks WHERE location = ?";
        String location = Parsers.locationToString(loc);
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, location);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "465cda2f", "Failed to unbind Interaction from block("+location+"): ", e
            ));
            return false;
        }
    }

    public String getBound(Location loc) {
        String sql = "SELECT interaction_id FROM inter_blocks WHERE location = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, Parsers.locationToString(loc));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("interaction_id");
            }
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "0d47d170", "Failed to get Block bound interaction: ", e
            ));
        }
        return null;
    }
    public String getNpcBound(int id) {
        String sql = "SELECT interaction_id FROM inter_npcs WHERE npc_id = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("interaction_id");
            }
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "af1a69b4", "Failed to get NPC bound interaction: ", e
            ));
        }
        return null;
    }


    public boolean interactionExists(String id, CommandSender s) {
        Interaction inter = getInteractionByID(id);
        if (inter != null) return true;
        Utils.error(s, "interactions", "error.not-exist");
        return false;
    }

}
