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
            String host = getConfig().getString("database.host");
            String port = getConfig().getString("database.port");
            String database = getConfig().getString("database.database");
            String username = getConfig().getString("database.username");
            String password = getConfig().getString("database.password");

            connection = DriverManager.getConnection(
                    "jdbc:mysql://" + host + ":" + port + "/" + database,
                    username,
                    password
            );
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
