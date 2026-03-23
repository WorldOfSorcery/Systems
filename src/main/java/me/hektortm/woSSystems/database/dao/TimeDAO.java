package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.database.SchemaManager;
import me.hektortm.woSSystems.utils.dataclasses.Activity;
import me.hektortm.wosCore.database.DatabaseManager;
import me.hektortm.wosCore.database.IDAO;
import me.hektortm.wosCore.discord.DiscordLog;
import me.hektortm.wosCore.discord.DiscordLogger;

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
        SchemaManager.syncTable(db, Activity.class);
    }
    public List<Activity> getAllActivities() {
        List<Activity> activities = new ArrayList<>();
        String sql = "SELECT * FROM activities";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String id = rs.getString("id");
                boolean isEnabled = rs.getBoolean("isEnabled");
                String name = rs.getString("name");
                boolean isDefault = rs.getBoolean("isDefault");
                String date = rs.getString("date");
                String message = rs.getString("message");
                int startTime = rs.getInt("start_time");
                int endTime = rs.getInt("end_time");
                String startInteraction = rs.getString("start_interaction");
                String endInteraction = rs.getString("end_interaction");
                Activity activity = new Activity(id, isEnabled, name, message, isDefault, date, startTime, endTime, startInteraction, endInteraction);
                activities.add(activity);
            }
            return activities;
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE, plugin, "5843eb20", "Failed to get all activities: ", e
            ));
            return new ArrayList<>();
        }
    }

}