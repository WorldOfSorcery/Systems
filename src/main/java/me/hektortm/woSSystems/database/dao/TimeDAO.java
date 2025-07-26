package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.dataclasses.Activity;
import me.hektortm.wosCore.database.DatabaseManager;
import me.hektortm.wosCore.database.IDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class TimeDAO implements IDAO {
    private final DatabaseManager db;
    private final DAOHub daoHub;
    private final WoSSystems plugin = WoSSystems.getPlugin(WoSSystems.class);
    private final String logName = "TimeDAO";

    public TimeDAO(DatabaseManager db, DAOHub daoHub) throws SQLException {
        this.db = db;
        this.daoHub = daoHub;
    }

    @Override
    public void initializeTable() throws SQLException {
        try (Connection conn = db.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS activities (
                    id VARCHAR(255) PRIMARY KEY,
                    name VARCHAR(255) NOT NULL,
                    message VARCHAR(255) NOT NULL,
                    isDefault BOOLEAN DEFAULT FALSE,
                    date VARCHAR(10),
                    start_time INT NOT NULL,
                    end_time INT NOT NULL,
                    start_interaction VARCHAR(255),
                    end_interaction VARCHAR(255)
                )
            """);
        }
    }

    public List<Activity> getActivity(int time, String inGameDate) {
        List<Activity> activities = new ArrayList<>();
        String sql = "SELECT * FROM activities WHERE start_time = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, time);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String id = rs.getString("id");
                String name = rs.getString("name");
                boolean isDefault = rs.getBoolean("default");
                String date = rs.getString("date");
                String message = rs.getString("message");
                int startTime = rs.getInt("start_time");
                int endTime = rs.getInt("end_time");
                String startInteraction = rs.getString("start_interaction");
                String endInteraction = rs.getString("end_interaction");
                if (isDefault || (Objects.equals(date, inGameDate))) {
                    Activity activity = new Activity(id, name, message, isDefault, date, startTime, endTime, startInteraction, endInteraction);
                    activities.add(activity);
                }
            } else {
                plugin.writeLog(logName, Level.INFO, "No activity found for date " + inGameDate + " and time " + time + ": " + "Returning null.");
                return null;
            }
            return activities;
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Error retrieving activity for date " + inGameDate + " and time " + time + ": " + e.getMessage());
            return null;
        }
    }

    public List<Activity> getAllActivities() {
        List<Activity> activities = new ArrayList<>();
        String sql = "SELECT * FROM activities";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String id = rs.getString("id");
                String name = rs.getString("name");
                boolean isDefault = rs.getBoolean("isDefault");
                String date = rs.getString("date");
                String message = rs.getString("message");
                int startTime = rs.getInt("start_time");
                int endTime = rs.getInt("end_time");
                String startInteraction = rs.getString("start_interaction");
                String endInteraction = rs.getString("end_interaction");
                Activity activity = new Activity(id, name, message, isDefault, date, startTime, endTime, startInteraction, endInteraction);
                activities.add(activity);
            }
            return activities;
        } catch (SQLException e) {
            plugin.writeLog(logName, Level.SEVERE, "Error retrieving all activities: " + e.getMessage());
            return null;
        }
    }

}
