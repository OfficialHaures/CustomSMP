package nl.inferno.customSMP.managers;

import nl.inferno.customSMP.CustomSMP;
import nl.inferno.customSMP.enums.ClanRank;
import nl.inferno.customSMP.models.Clan;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ClanManager {
    private final CustomSMP plugin;
    private final Map<String, Clan> clans = new HashMap<>();
    private final Map<UUID, String> playerClans = new HashMap<>();

    public ClanManager(CustomSMP plugin) {
        this.plugin = plugin;
        loadClans();
    }

    private void loadClans() {
        try {
            PreparedStatement stmt = plugin.getConnection().prepareStatement(
                    "SELECT * FROM clans"
            );
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Clan clan = new Clan(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("tag"),
                        UUID.fromString(rs.getString("leader"))
                );
                clans.put(clan.getName().toLowerCase(), clan);
                loadClanMembers(clan);
                loadClanData(clan);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error loading clans: " + e.getMessage());
        }
    }

    private void loadClanMembers(Clan clan) {
        try {
            PreparedStatement stmt = plugin.getConnection().prepareStatement(
                    "SELECT * FROM clan_members WHERE clan_id = ?"
            );
            stmt.setInt(1, clan.getId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                UUID playerId = UUID.fromString(rs.getString("player_uuid"));
                ClanRank rank = ClanRank.valueOf(rs.getString("rank"));
                clan.getMemberRanks().put(playerId, rank);
                clan.getMembers().add(playerId);
                if (rank == ClanRank.MODERATOR) {
                    clan.getModerators().add(playerId);
                }
                playerClans.put(playerId, clan.getName().toLowerCase());
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error loading clan members: " + e.getMessage());
        }
    }

    private void loadClanData(Clan clan) {
        try {
            PreparedStatement stmt = plugin.getConnection().prepareStatement(
                    "SELECT * FROM clan_data WHERE clan_id = ?"
            );
            stmt.setInt(1, clan.getId());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                clan.setLevel(rs.getInt("level"));
                clan.setExperience(rs.getInt("experience"));
                clan.setBalance(rs.getDouble("balance"));
                clan.setMaxMembers(rs.getInt("max_members"));
                clan.setMaxTerritories(rs.getInt("max_territories"));

                String homeWorld = rs.getString("home_world");
                if (homeWorld != null) {
                    clan.setHome(new Location(
                            Bukkit.getWorld(homeWorld),
                            rs.getDouble("home_x"),
                            rs.getDouble("home_y"),
                            rs.getDouble("home_z"),
                            rs.getFloat("home_yaw"),
                            rs.getFloat("home_pitch")
                    ));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error loading clan data: " + e.getMessage());
        }
    }

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
                        playerClans.put(leader.getUniqueId(), clanName.toLowerCase());
                        initializeClanData(clan);
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

    private void initializeClanData(Clan clan) throws SQLException {
        PreparedStatement stmt = plugin.getConnection().prepareStatement(
                "INSERT INTO clan_data (clan_id, level, experience, balance, max_members, max_territories) " +
                        "VALUES (?, 1, 0, 0, 10, 5)"
        );
        stmt.setInt(1, clan.getId());
        stmt.executeUpdate();
    }

    public void invitePlayer(Clan clan, Player inviter, Player target) {
        if (getPlayerClan(target.getUniqueId()) != null) {
            inviter.sendMessage(ChatColor.RED + "Deze speler zit al in een clan!");
            return;
        }

        clan.getPendingInvites().put(target.getUniqueId(), new Clan.ClanInvite(inviter.getUniqueId(), target.getUniqueId()));

        TextComponent message = new TextComponent(ChatColor.GREEN + "Je bent uitgenodigd voor clan " + clan.getName() + "! ");
        TextComponent accept = new TextComponent(ChatColor.YELLOW + "[Accept]");
        accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/clan join " + clan.getName()));

        target.spigot().sendMessage(message, accept);
        inviter.sendMessage(ChatColor.GREEN + "Uitnodiging verstuurd naar " + target.getName());
    }

    public void acceptInvite(Player player, String clanName) {
        Clan clan = getClanByName(clanName);
        if (clan == null) {
            player.sendMessage(ChatColor.RED + "Deze clan bestaat niet!");
            return;
        }

        Clan.ClanInvite invite = clan.getPendingInvites().get(player.getUniqueId());
        if (invite == null || invite.isExpired()) {
            player.sendMessage(ChatColor.RED + "Je hebt geen geldige uitnodiging voor deze clan!");
            return;
        }

        addMemberToClan(clan, player.getUniqueId());
        clan.getPendingInvites().remove(player.getUniqueId());
        clan.broadcast(ChatColor.GREEN + player.getName() + " is toegetreden tot de clan!");
    }

    private void addMemberToClan(Clan clan, UUID playerId) {
        try {
            PreparedStatement stmt = plugin.getConnection().prepareStatement(
                    "INSERT INTO clan_members (clan_id, player_uuid, rank) VALUES (?, ?, ?)"
            );
            stmt.setInt(1, clan.getId());
            stmt.setString(2, playerId.toString());
            stmt.setString(3, ClanRank.RECRUIT.name());
            stmt.executeUpdate();

            clan.addMember(playerId);
            playerClans.put(playerId, clan.getName().toLowerCase());
        } catch (SQLException e) {
            plugin.getLogger().severe("Error adding member to clan: " + e.getMessage());
        }
    }

    public void removeMember(Clan clan, UUID playerId) {
        try {
            PreparedStatement stmt = plugin.getConnection().prepareStatement(
                    "DELETE FROM clan_members WHERE clan_id = ? AND player_uuid = ?"
            );
            stmt.setInt(1, clan.getId());
            stmt.setString(2, playerId.toString());
            stmt.executeUpdate();

            clan.removeMember(playerId);
            playerClans.remove(playerId);
        } catch (SQLException e) {
            plugin.getLogger().severe("Error removing member from clan: " + e.getMessage());
        }
    }

    public void promoteMember(Clan clan, UUID playerId) {
        try {
            clan.promoteMember(playerId);
            updateMemberRank(clan, playerId, clan.getMemberRank(playerId));

            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                player.sendMessage(ChatColor.GREEN + "Je bent gepromoveerd naar " +
                        clan.getMemberRank(playerId).name() + "!");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error promoting member: " + e.getMessage());
        }
    }

    private void updateMemberRank(Clan clan, UUID playerId, ClanRank rank) throws SQLException {
        PreparedStatement stmt = plugin.getConnection().prepareStatement(
                "UPDATE clan_members SET rank = ? WHERE clan_id = ? AND player_uuid = ?"
        );
        stmt.setString(1, rank.name());
        stmt.setInt(2, clan.getId());
        stmt.setString(3, playerId.toString());
        stmt.executeUpdate();
    }

    public void updateClanHome(Clan clan) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                PreparedStatement stmt = plugin.getConnection().prepareStatement(
                        "UPDATE clan_data SET home_world = ?, home_x = ?, home_y = ?, home_z = ?, home_yaw = ?, home_pitch = ? WHERE clan_id = ?"
                );
                Location home = clan.getHome();
                stmt.setString(1, home.getWorld().getName());
                stmt.setDouble(2, home.getX());
                stmt.setDouble(3, home.getY());
                stmt.setDouble(4, home.getZ());
                stmt.setFloat(5, home.getYaw());
                stmt.setFloat(6, home.getPitch());
                stmt.setInt(7, clan.getId());
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Error updating clan home: " + e.getMessage());
            }
        });
    }

    public void updateClanExperience(Clan clan) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                PreparedStatement stmt = plugin.getConnection().prepareStatement(
                        "UPDATE clan_data SET experience = ?, level = ? WHERE clan_id = ?"
                );
                stmt.setInt(1, clan.getExperience());
                stmt.setInt(2, clan.getLevel());
                stmt.setInt(3, clan.getId());
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Error updating clan experience: " + e.getMessage());
            }
        });
    }

    public void updateClanBalance(Clan clan) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                PreparedStatement stmt = plugin.getConnection().prepareStatement(
                        "UPDATE clan_data SET balance = ? WHERE clan_id = ?"
                );
                stmt.setDouble(1, clan.getBalance());
                stmt.setInt(2, clan.getId());
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Error updating clan balance: " + e.getMessage());
            }
        });
    }

    public CompletableFuture<Boolean> disbandClan(Clan clan) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                plugin.getConnection().prepareStatement(
                        "DELETE FROM clan_data WHERE clan_id = " + clan.getId()
                ).executeUpdate();

                plugin.getConnection().prepareStatement(
                        "DELETE FROM clan_members WHERE clan_id = " + clan.getId()
                ).executeUpdate();

                plugin.getConnection().prepareStatement(
                        "DELETE FROM clans WHERE id = " + clan.getId()
                ).executeUpdate();

                clans.remove(clan.getName().toLowerCase());
                clan.getMembers().forEach(playerClans::remove);

                return true;
            } catch (SQLException e) {
                plugin.getLogger().severe("Error disbanding clan: " + e.getMessage());
                return false;
            }
        });
    }

    public Clan getPlayerClan(UUID playerId) {
        String clanName = playerClans.get(playerId);
        return clanName != null ? clans.get(clanName.toLowerCase()) : null;
    }

    public Clan getClanByName(String name) {
        return clans.get(name.toLowerCase());
    }

    public void demoteMember(Clan clan, UUID playerId) {
        try {
            clan.demoteMember(playerId);
            updateMemberRank(clan, playerId, clan.getMemberRank(playerId));

            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                player.sendMessage(ChatColor.YELLOW + "Je bent gedegradeerd naar " +
                        clan.getMemberRank(playerId).name() + ".");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error demoting member: " + e.getMessage());
        }
    }

    public Map<String, Clan> getClans() {
        return clans;
    }
}
