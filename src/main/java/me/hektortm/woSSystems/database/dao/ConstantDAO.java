package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.dataclasses.Constant;
import me.hektortm.wosCore.database.DatabaseManager;
import me.hektortm.wosCore.database.IDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class ConstantDAO implements IDAO {
    private final DatabaseManager db;
    private final DAOHub daoHub;
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final String logName = "ConstantDAO";

    public ConstantDAO(DatabaseManager db, DAOHub daoHub) {
        this.db = db;
        this.daoHub = daoHub;
    }

    @Override
    public void initializeTable() throws SQLException {
        try (Connection conn = db.getConnection(); Statement stmt = conn.createStatement()) {

            String sql = """
                        CREATE TABLE IF NOT EXISTS constants (
                            id VARCHAR(255) NOT NULL,
                            value VARCHAR(255) NOT NULL)
                        """;
            stmt.execute(sql);
        }
    }

    public List<Constant> getAllConstants() {
        String sql = "SELECT * FROM constants";
        List<Constant> constants = new ArrayList<>();

        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeQuery();
            ResultSet rs = pstmt.getResultSet();
            while (rs.next()) {
                constants.add(new Constant(rs.getString("id"), rs.getString("value")));
            }
            return constants;
        } catch (SQLException ex) {
            plugin.writeLog(logName, Level.SEVERE, "Could not get all constants");
            return null;
        }
    }

    public Constant getConstantById(String id) {
        String sql = "SELECT * FROM constants WHERE id = ?";
        Constant constant = null;
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                constant = new Constant(rs.getString("id"), rs.getString("value"));
            }
            return constant;
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Could not get constant by id");
            return null;
        }
    }


}
