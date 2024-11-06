package nl.inferno.customSMP.managers;

import nl.inferno.customSMP.CustomSMP;
import nl.inferno.customSMP.models.Clan;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ClanManager {
    private CustomSMP plugin;
    private final Map<String, Clan> clans = new HashMap<>();

    public CompletableFuture<Boolean> createClan(Player leader, String clanName, String tag) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                PreparedStatement stmt = plugin.getConnection().prepareStatement(
                    "INSERT INTO clans (name, tag, leader) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
                );

                stmt.setString(1, clanName);
                stmt.setString(2, tag);
                stmt.setString(3, leader.getUniqueId().toString());

                int affectedRows = stmt.executeUpdate();

                if (affectedRows > 0) {
                    ResultSet rs = stmt.getGeneratedKeys();
                    if (rs.next()) {
                        int clanId = rs.getInt(1);
                        Clan clan = new Clan(clanId, clanName, tag, leader.getUniqueId());
                        clans.put(clanName.toLowerCase(), clan);
                        return true;
                    }
                }
                return false;
            } catch (SQLException e) {
                plugin.getLogger().severe("Error creating clan: " + e.getMessage());
                return false;
            }
        });
    }
}
