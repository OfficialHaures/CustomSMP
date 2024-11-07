package nl.inferno.customSMP;

import nl.inferno.customSMP.commands.ClanCommand;
import nl.inferno.customSMP.managers.ClanManager;
import nl.inferno.customSMP.managers.PermissionManager;
import nl.inferno.customSMP.systems.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class CustomSMP extends JavaPlugin {
    private Connection connection;
    private static CustomSMP instance;
    private ClanManager clanManager;
    private LevelSystem levelSystem;
    private ClanWarSystem clanWarSystem;
    private TerritorySystem territorySystem;
    private PermissionManager permissionManager;
    private ClanBankSystem clanBankSystem;
    private AchievementSystem achievementSystem;
    private ClanChatSystem clanChatSystem;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        connectToDatabase();
        initializeSystems();
        registerCommands();
        registerListeners();
    }

    private void initializeSystems() {
        clanManager = new ClanManager(this);
        levelSystem = new LevelSystem(this);
        clanWarSystem = new ClanWarSystem(this);
        territorySystem = new TerritorySystem(this);
        clanBankSystem = new ClanBankSystem(this);
        achievementSystem = new AchievementSystem(this);
        clanChatSystem = new ClanChatSystem(this);
    }

    private void registerCommands() {
        getCommand("clan").setExecutor(new ClanCommand(this));
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(clanChatSystem, this);
    }

    @Override
    public void onDisable() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                getLogger().severe("Error closing database connection: " + e.getMessage());
            }
        }
    }

    private void connectToDatabase() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(
                    "jdbc:mysql://u11_1IMZSyQUhi:cnmkYkdZNyREvkSWkbc%40%3D0Ml@185.228.82.169:3306/s11_SMPClans",
                    "u11_1IMZSyQUhi",
                    "cnmkYkdZNyREvkSWkbc@=0Ml"
            );

            // Create clans table
            connection.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS clans (" +
                            "id INT PRIMARY KEY AUTO_INCREMENT," +
                            "name VARCHAR(32) UNIQUE," +
                            "tag VARCHAR(5)," +
                            "leader VARCHAR(36)," +
                            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
            );

            // Create clan_members table
            connection.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS clan_members (" +
                            "clan_id INT," +
                            "player_uuid VARCHAR(36)," +
                            "rank VARCHAR(16)," +
                            "joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                            "PRIMARY KEY (clan_id, player_uuid)," +
                            "FOREIGN KEY (clan_id) REFERENCES clans(id) ON DELETE CASCADE)"
            );

            // Create clan_data table
            connection.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS clan_data (" +
                            "clan_id INT PRIMARY KEY," +
                            "level INT DEFAULT 1," +
                            "experience INT DEFAULT 0," +
                            "balance DOUBLE DEFAULT 0," +
                            "max_members INT DEFAULT 10," +
                            "max_territories INT DEFAULT 5," +
                            "home_world VARCHAR(64)," +
                            "home_x DOUBLE," +
                            "home_y DOUBLE," +
                            "home_z DOUBLE," +
                            "home_yaw FLOAT," +
                            "home_pitch FLOAT," +
                            "FOREIGN KEY (clan_id) REFERENCES clans(id) ON DELETE CASCADE)"
            );

            // Add any missing columns to existing tables
            connection.createStatement().execute(
                    "ALTER TABLE clans ADD COLUMN IF NOT EXISTS tag VARCHAR(5) AFTER name"
            );

            getLogger().info("Database tables initialized successfully!");

        } catch (Exception e) {
            getLogger().severe("Database connection failed: " + e.getMessage());
        }
    }



    public static CustomSMP getInstance() {
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    // Getters for all systems
    public ClanManager getClanManager() { return clanManager; }
    public LevelSystem getLevelSystem() { return levelSystem; }
    public ClanWarSystem getClanWarSystem() { return clanWarSystem; }
    public TerritorySystem getTerritorySystem() { return territorySystem; }
    public ClanBankSystem getClanBankSystem() { return clanBankSystem; }
    public AchievementSystem getAchievementSystem() { return achievementSystem; }
    public ClanChatSystem getClanChatSystem() { return clanChatSystem; }
    public PermissionManager getPermissionManager() { return permissionManager; }
}
