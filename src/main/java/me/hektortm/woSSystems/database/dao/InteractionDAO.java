package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
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
        try (Connection conn = db.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS interactions (
                    id VARCHAR(255) NOT NULL,
                    PRIMARY KEY (id)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS inter_actions (
                    id VARCHAR(255) NOT NULL,
                    behaviour VARCHAR(255) NOT NULL,
                    matchtype VARCHAR(255) NOT NULL,
                    action_id INT NOT NULL,
                    actions TEXT NOT NULL,
                    PRIMARY KEY (id, action_id),
                    FOREIGN KEY (id) REFERENCES interactions(id)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS inter_holograms (
                    interaction_id VARCHAR(255) NOT NULL,
                    behaviour VARCHAR(255) NOT NULL,
                    matchtype VARCHAR(255) NOT NULL,
                    hologram_id INT NOT NULL,
                    hologram TEXT NOT NULL,
                    PRIMARY KEY (interaction_id),
                    FOREIGN KEY (interaction_id) REFERENCES interactions(id)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS inter_npcs (
                    npc_id VARCHAR(255) NOT NULL,
                    interaction_id VARCHAR(255) NOT NULL,
                    PRIMARY KEY (interaction_id),
                    FOREIGN KEY (interaction_id) REFERENCES interactions(id)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS inter_blocks (
                    location TEXT NOT NULL,
                    interaction_id VARCHAR(255) NOT NULL,
                    PRIMARY KEY (interaction_id),
                    FOREIGN KEY (interaction_id) REFERENCES interactions(id)
                )
            """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS inter_particles (
                    id VARCHAR(255) NOT NULL,
                    behaviour VARCHAR(255) NOT NULL,
                    matchtype VARCHAR(255) NOT NULL,
                    particle_id VARCHAR(255) NOT NULL,
                    particle VARCHAR(255) NOT NULL,
                    particle_color VARCHAR(255),
                    PRIMARY KEY (id),
                    FOREIGN KEY (id) REFERENCES interactions(id)
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
                String behaviour = rs.getString("behaviour");
                String matchType = rs.getString("matchtype");
                int actionId = rs.getInt("action_id");
                String actionsRaw = rs.getString("actions");

                // Assuming actions are stored like ["cmd1", "cmd2"]
                List<String> parsedActions = Arrays.stream(actionsRaw.replace("[", "").replace("]", "").split(","))
                        .map(String::trim)
                        .map(s -> s.replaceAll("^\"|\"$", "")) // remove surrounding quotes
                        .collect(Collectors.toList());

                actions.add(new InteractionAction(interactionId, behaviour, matchType, actionId, parsedActions));
            }

        } catch (SQLException e) {
            e.printStackTrace();
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
                String behaviour = rs.getString("behaviour");
                String matchType = rs.getString("matchtype");
                int particleId = rs.getInt("particle_id");
                String particle = rs.getString("particle");
                String particleColor = rs.getString("particle_color");

                particles.add(new InteractionParticles(id, behaviour, matchType, particleId, particle, particleColor));
            }
            return particles;
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Failed to get particles: " + e);
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
                String behaviour = rs.getString("behaviour");
                String matchType = rs.getString("matchtype");
                String hologramRaw = rs.getString("hologram");

                // Assuming holograms are stored like ["line1", "line2"]
                List<String> parsedHologram = Arrays.stream(hologramRaw.replace("[", "").replace("]", "").split(","))
                        .map(String::trim)
                        .map(s -> s.replaceAll("^\"|\"$", "")) // remove surrounding quotes
                        .collect(Collectors.toList());
                holograms.add(new InteractionHologram(id, hologramId, behaviour, matchType, parsedHologram));
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
            plugin.writeLog(logName, Level.SEVERE, "Failed to bind NPC: " + e);
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
            plugin.writeLog(logName, Level.SEVERE, "Failed to get NPCs: " + e);
        }
        return npcs;
    }

    public String getNPCInteraction(Location loc) {
        String location = Parsers.locationToString(loc);
        String sql = "SELECT interaction_id FROM inter_npcs WHERE npc_id = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, location);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("interaction_id");
            }
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Failed to get NPC interaction: " + e);
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
            plugin.writeLog(logName, Level.SEVERE, "Failed to bind block: " + e);
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
            plugin.writeLog(logName, Level.SEVERE, "Failed to get blocks: " + e);
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
            plugin.writeLog(logName, Level.SEVERE, "Failed to get all block locations: " + e);
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
            plugin.writeLog(logName, Level.SEVERE, "Failed to get block interaction: " + e);
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
            plugin.writeLog(logName, Level.SEVERE, "Failed to get interaction: " + e);
        }
        return null;
    }

    public List<Interaction> getInteractions() {
        List<Interaction> interactions = new ArrayList<>();
        String sql = "SELECT * FROM interactions";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String interactionId = rs.getString("id");
                interactions.add(getInteractionByID(interactionId));
            }
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Failed to get interactions: " + e);
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
                    Level.SEVERE, plugin, "TODO", "Could not unbind Interaction from Npc("+id+"): ", e
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
                    Level.SEVERE, plugin, "TODO", "Could not unbind Interaction from block("+location+"): ", e
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
                    Level.SEVERE, plugin, "TODO", "Could not get Block bound interaction: ", e
            ));
        }
        return null;
    }
    public String getNpcBound(int id) {
        String sql = "SELECT interaction_id FROM inter_npc WHERE npc_id = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("interaction_id");
            }
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "TODO", "Could not get NPC bound interaction: ", e
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
