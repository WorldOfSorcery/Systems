package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.WoSSystems;
import me.hektortm.woSSystems.database.DAOHub;
import me.hektortm.woSSystems.utils.dataclasses.BasicCommand;
import me.hektortm.wosCore.database.DatabaseManager;
import me.hektortm.wosCore.database.IDAO;
import me.hektortm.wosCore.discord.DiscordLog;
import me.hektortm.wosCore.discord.DiscordLogger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class CommandsDAO implements IDAO {

    private final WoSSystems plugin = WoSSystems.getInstance();
    private DatabaseManager db = plugin.getCore().getDatabaseManager();
    private final DAOHub daoHub;

    public CommandsDAO(DAOHub daoHub) {
        this.daoHub = daoHub;
    }


    @Override
    public void initializeTable() throws SQLException {
        try (Connection conn = db.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS commands(
                    command VARCHAR(255) NOT NULL,
                    permission VARCHAR(255) NULL,
                    interaction VARCHAR(255) NOT NULL,
                    PRIMARY KEY (command)
                )
            """);
        }
    }

    public List<BasicCommand> getCommands() {
        String sql = "SELECT * FROM commands";
        List<BasicCommand> commands = new ArrayList<>();
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeQuery();
            ResultSet rs = pstmt.getResultSet();
            while (rs.next()) {
                commands.add(new BasicCommand(rs.getString("command"), rs.getString("permission"), rs.getString("interaction")));
            }
            return commands;
        } catch (SQLException e) {
            DiscordLogger.log(new DiscordLog(
                    Level.SEVERE,
                    plugin,
                    "c32u6t",
                    "Failed to get Commands",
                    e
            ));
        }

        return commands;
    }

}
