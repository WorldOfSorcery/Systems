package me.hektortm.woSSystems.database.dao;

import me.hektortm.woSSystems.database.DatabaseManager;

import java.sql.Connection;
import java.sql.Statement;

public class ChannelDAO {
    private final Connection conn;

    public ChannelDAO(DatabaseManager db) {
        this.conn = db.getConnection();
        createTables();
    }

    private void createTables() {
        try (Statement stmt = conn.createStatement()) {

            stmt.execute("""
            CREATE TABLE IF NOT EXISTS channels (
            name TEXT PRIMARY KEY NOT NULL,
            short_name TEXT NOT NULL,
            prefix TEXT NOT NULL,
            format TEXT NOT NULL,
            autojoin BOOLEAN NOT NULL,
            forcejoin BOOLEAN NOT NULL,
            )
""");
            stmt.execute("""
            CREATE TABLE IF NOT EXISTS playerdata_channels (
            uuid TEXT PRIMARY KEY NOT NULL,
            channel_name TEXT NOT NULL,
            joined BOOLEAN NOT NULL,
            focused BOOLEAN NOT NULL,
            )
""");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
