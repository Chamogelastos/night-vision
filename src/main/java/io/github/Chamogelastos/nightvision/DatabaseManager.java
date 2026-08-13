package io.github.Chamogelastos.nightvision;

import java.io.File;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DatabaseManager {
    private final File dataFolder;

    private Connection connection;

    public DatabaseManager(File dataFolder) {
        this.dataFolder = dataFolder;

        try {
            connect();
            createTable();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize the database.", e);
        }
    }

    private void connect() {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        var dbFile = new File(dataFolder, "players.db");
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void createTable() {
        var sql = "CREATE TABLE IF NOT EXISTS players (uuid TEXT PRIMARY KEY NOT NULL);";
        try {
            connection.createStatement().execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeNightVisionUser(UUID uuid) {
        var sql = "DELETE FROM players WHERE uuid = ?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }


    public Set<UUID> getNightVisionUsers() {
        Set<UUID> users = new HashSet<>();
        try (PreparedStatement stmt = connection.prepareStatement("SELECT uuid FROM players;")) {
            stmt.executeUpdate();
            ResultSet rs = stmt.getResultSet();
            while (rs.next()) {
                users.add(UUID.fromString(rs.getString("uuid")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public void closeConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}