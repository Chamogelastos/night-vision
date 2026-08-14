package io.github.Chamogelastos.nightvision;

import java.io.File;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class DatabaseManager {

    private final NightVision plugin;

    private final Connection connection;

    public DatabaseManager(NightVision plugin, File dataFolder) {
        this.plugin = plugin;

        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        var dbFile = new File(dataFolder, "players.db");

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            createTable();
        } catch (SQLException | ClassNotFoundException e) {
            throw new IllegalStateException("Failed to initialize the database.", e);
        }
    }

    private void createTable() throws SQLException {
        var sql = "CREATE TABLE IF NOT EXISTS players (uuid TEXT PRIMARY KEY NOT NULL);";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    public void addNightVisionUser(UUID uuid) {
        CompletableFuture.runAsync(() -> {
            var sql = "INSERT OR IGNORE INTO players (uuid) VALUES (?);";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, uuid.toString());
                stmt.executeUpdate();
            } catch (SQLException ex) {
                plugin.getLogger().severe("Failed to add user to database: " + ex.getMessage());
            }
        }, Executors.newVirtualThreadPerTaskExecutor());
    }


    public void removeNightVisionUser(UUID uuid) {
        CompletableFuture.runAsync(() -> {
            var sql = "DELETE FROM players WHERE uuid = ?;";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, uuid.toString());
                stmt.executeUpdate();
            } catch (SQLException ex) {
                plugin.getLogger().severe("Failed to remove user from database: " + ex.getMessage());
            }
        }, Executors.newVirtualThreadPerTaskExecutor());
    }

    public CompletableFuture<Set<UUID>> getNightVisionUsers() {
        return CompletableFuture.supplyAsync(() -> {
            Set<UUID> users = new HashSet<>();
            try (PreparedStatement stmt = connection.prepareStatement("SELECT uuid FROM players;");
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(UUID.fromString(rs.getString("uuid")));
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to load users from database: " + e.getMessage());
            }
            return users;
        }, Executors.newVirtualThreadPerTaskExecutor());
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}