package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.family.Family;
import me.hektortm.woSSystems.family.FamilyMember;
import me.hektortm.woSSystems.family.Role;
import me.hektortm.wosCore.database.DatabaseManager;
import me.hektortm.wosCore.database.IDAO;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class FamilyDAO implements IDAO {
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final DatabaseManager db;
    private final DAOHub hub;
    private final String logName = "FamilyDAO";

    public FamilyDAO(DatabaseManager db, DAOHub hub) {
        this.db = db;
        this.hub = hub;
    }

    @Override
    public void initializeTable() throws SQLException {
        try (Connection conn = db.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS family (
                        id VARCHAR(255) UNIQUE NOT NULL,
                        owner_uuid VARCHAR(255) NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        PRIMARY KEY (id)
                    )
                    """);
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS family_members (
                        family_id VARCHAR(255) NOT NULL,
                        member_uuid VARCHAR(255) NOT NULL,
                        prefix VARCHAR(50) DEFAULT '',
                        role VARCHAR(50) DEFAULT 'member',
                        FOREIGN KEY (family_id) REFERENCES family(id) ON DELETE CASCADE
                    )
                   """);
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS playerdata_families (
                        player_uuid VARCHAR(255) NOT NULL,
                        family_id VARCHAR(255) NOT NULL,
                        active BOOLEAN DEFAULT FALSE,
                        FOREIGN KEY (family_id) REFERENCES family(id) ON DELETE CASCADE,
                        PRIMARY KEY (player_uuid, family_id)
                    )
                   """);
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS family_invites (
                        family_id VARCHAR(255) NOT NULL,
                        player_uuid VARCHAR(255) NOT NULL,
                        invited_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (family_id) REFERENCES family(id) ON DELETE CASCADE,
                        PRIMARY KEY (family_id, player_uuid)
                    )
                   """);
        }
    }

    public List<FamilyMember> getFamilyMembers(String id) {
        String sql = "SELECT * FROM family_members WHERE family_id = ?";

        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (var rs = stmt.executeQuery()) {
                List<FamilyMember> members = new ArrayList<>();
                while (rs.next()) {
                    String memberUuid = rs.getString("member_uuid");
                    String role = rs.getString("role");
                    String prefix = rs.getString("prefix");
                    members.add(new FamilyMember(UUID.fromString(memberUuid), Role.fromString(role), prefix));
                }
                return members;
            }
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Error retrieving family members for family ID: " + id + "\n" + e);
            return null;
        }// Placeholder return statement
    }

    public Family getFamily(String familyId) {
        String sql = "SELECT * FROM family WHERE id = ?";

        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, familyId);
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String id = rs.getString("id");
                    UUID ownerUuid = UUID.fromString(rs.getString("owner_uuid"));
                    List<FamilyMember> members = getFamilyMembers(id);
                    return new Family(id, id.replace("_", ""), ownerUuid, members);
                }
            }
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Error retrieving family with ID: " + familyId + "\n" + e);
        }

        return null;
    }

    public void createFamily(UUID ownerUuid, String familyName) {
        String sql = "INSERT INTO family (id, owner_uuid) VALUES (?, ?)";

        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, familyName);
            stmt.setString(2, ownerUuid.toString());
            stmt.executeUpdate();
            addPlayerToFamilyMembers(familyName, ownerUuid, "", Role.HEAD);
            setActiveFamilyForPlayer(familyName, ownerUuid);
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Error creating family: " + e);
        }
    }

    public Family getPlayersActiveFamily(UUID playerUuid) {
        String sql = "SELECT family_id FROM playerdata_families WHERE player_uuid = ? AND active = TRUE";

        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerUuid.toString());
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String familyId = rs.getString("family_id");
                    return getFamily(familyId);
                }
            }
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Error retrieving active family for player UUID: " + playerUuid + "\n" + e);
        }

        return null;
    }

    public void invitePlayerToFamily(String familyId, UUID playerUuid) {
        String sql = "INSERT INTO family_invites (family_id, player_uuid) VALUES (?, ?)";

        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, familyId);
            stmt.setString(2, playerUuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Error inviting player to family: " + e);
        }
    }

    public void rejectInvite(String familyId, UUID playerUuid) {
        String sql = "DELETE FROM family_invites WHERE family_id = ? AND player_uuid = ?";

        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, familyId);
            stmt.setString(2, playerUuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Error rejecting invite: " + e);
        }
    }
    public void acceptInvite(String familyId, UUID playerUuid) {
        String sql = "INSERT INTO playerdata_families (player_uuid, family_id, active) VALUES (?, ?, TRUE)";

        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerUuid.toString());
            stmt.setString(2, familyId);
            stmt.executeUpdate();
            addPlayerToFamilyMembers(familyId, playerUuid, "", Role.MEMBER);
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Error accepting invite: " + e);
        }
    }

    public void leaveFamily(String familyId, UUID playerUuid) {
        String sql = "DELETE FROM playerdata_families WHERE family_id = ? AND player_uuid = ?";

        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, familyId);
            stmt.setString(2, playerUuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Error leaving family: " + e);
        }
    }

    public void setActiveFamilyForPlayer(String familyId, UUID playerUuid) {
        String sql = "UPDATE playerdata_families SET active = TRUE WHERE family_id = ? AND player_uuid = ?";

        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, familyId);
            stmt.setString(2, playerUuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Error setting active family for player: " + e);
        }
    }

    public boolean isInFamily(String familyId, UUID playerUuid) {
        String sql = "SELECT COUNT(*) FROM playerdata_families WHERE family_id = ? AND player_uuid = ?";

        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, familyId);
            stmt.setString(2, playerUuid.toString());
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Error checking if player is in family: " + e);
        }
        return false;
    }

    public void addPlayerToFamilyMembers(String familyId, UUID playerUuid, String prefix, Role role) {
        String sql = "INSERT INTO family_members (family_id, member_uuid, prefix, role) VALUES (?, ?, ?, ?)";

        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, familyId);
            stmt.setString(2, playerUuid.toString());
            stmt.setString(3, prefix);
            stmt.setString(4, role.name());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Error adding player to family members: " + e);
        }
    }


    public List<String> getRequests(UUID playerUuid) {
        String sql = "SELECT family_id FROM family_invites WHERE player_uuid = ?";
        List<String> requests = new ArrayList<>();

        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerUuid.toString());
            try (var rs = stmt.executeQuery()) {
                while (rs.next()) {
                    requests.add(rs.getString("family_id"));
                }
            }
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Error retrieving requests for player UUID: " + playerUuid + "\n" + e);
        }

        return requests;
    }
}
