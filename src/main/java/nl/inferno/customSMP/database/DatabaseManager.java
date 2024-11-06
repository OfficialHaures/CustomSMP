package nl.inferno.customSMP.database;

import nl.inferno.customSMP.CustomSMP;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseManager {
    private final CustomSMP plugin;
    private final Connection connection;

    public DatabaseManager(CustomSMP plugin) {
        this.plugin = plugin;
        this.connection = plugin.getConnection();
        createTables();
    }

    private void createTables() {
        try {
            connection.createStatement().execute("""
                CREATE TABLE IF NOT EXISTS clans (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    name VARCHAR(32) UNIQUE,
                    tag VARCHAR(8),
                    leader VARCHAR(36),
                    description TEXT,
                    max_members INT DEFAULT 10,
                    balance DOUBLE DEFAULT 0,
                    home_world VARCHAR(64),
                    home_x DOUBLE,
                    home_y DOUBLE,
                    home_z DOUBLE,
                    level INT DEFAULT 1,
                    experience INT DEFAULT 0,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            connection.createStatement().execute("""
                CREATE TABLE IF NOT EXISTS clan_members (
                    clan_id INT,
                    player_uuid VARCHAR(36),
                    rank VARCHAR(32),
                    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (clan_id, player_uuid),
                    FOREIGN KEY (clan_id) REFERENCES clans(id) ON DELETE CASCADE
                )
            """);
        } catch (SQLException e) {
            plugin.getLogger().severe("Error creating tables: " + e.getMessage());
        }
    }
}
