package com.artillexstudios.axcalendar.database.impl;

import com.artillexstudios.axcalendar.AxCalendar;
import com.artillexstudios.axcalendar.database.Database;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

public class SQLite implements Database {
    private Connection conn;

    @Override
    public String getType() {
        return "SQLite";
    }

    @Override
    public void setup() {
        try {
            Class.forName("org.sqlite.JDBC");
            this.conn = DriverManager.getConnection(String.format("jdbc:sqlite:%s/data.db", AxCalendar.getInstance().getDataFolder()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        final String CREATE_TABLE = """
                        CREATE TABLE axcalendar_data (
                        	`uuid` VARCHAR(36) NOT NULL,
                        	`day` INT(64) NOT NULL
                        );
                """;

        try (PreparedStatement stmt = conn.prepareStatement(CREATE_TABLE)) {
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void claim(@NotNull UUID uuid, int day) {

        final String sql = """
                        INSERT INTO axcalendar_data (uuid, day) VALUES (?, ?);
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setInt(2, day);

            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean isClaimed(@NotNull UUID uuid, int day) {

        final String sql = """
                        SELECT * FROM axcalendar_data WHERE uuid = ? AND day = ? LIMIT 1;
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            return rs.next();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    @Override
    public ArrayList<Integer> claimedDays(@NotNull UUID uuid) {
        final ArrayList<Integer> claimedDays = new ArrayList<>();

        final String sql = """
                        SELECT day FROM axcalendar_data WHERE uuid = ?;
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) claimedDays.add(rs.getInt(1));
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return claimedDays;
    }

    @Override
    public void disable() {
        try {
            conn.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}