package nl.inferno.customSMP.models;

import nl.inferno.customSMP.CustomSMP;
import nl.inferno.customSMP.enums.ClanRank;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

public class Clan {
    private final int id;
    private String name;
    private String tag;
    private UUID leader;
    private int level = 1;
    private int experience = 0;
    private double balance = 0;
    private Location home;
    private int maxMembers = 10;
    private int maxTerritories = 5;
    private double bankLimit = 10000;
    private final Set<UUID> members = new HashSet<>();
    private final Set<UUID> moderators = new HashSet<>();
    private final Map<UUID, ClanRank> memberRanks = new HashMap<>();
    private final Set<String> achievements = new HashSet<>();
    private final Map<UUID, ClanInvite> pendingInvites = new HashMap<>();
    private final CustomSMP plugin;

    public Clan(int id, String name, String tag, UUID leader) {
        this.id = id;
        this.name = name;
        this.tag = tag;
        this.leader = leader;
        this.plugin = CustomSMP.getInstance();
        this.memberRanks.put(leader, ClanRank.LEADER);
        this.members.add(leader);
    }

    public Set<UUID> getModerators() {
        return moderators;
    }

    public void setTag(String newTag) {
        this.tag = newTag;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                PreparedStatement stmt = plugin.getConnection().prepareStatement(
                        "UPDATE clans SET tag = ? WHERE id = ?"
                );
                stmt.setString(1, newTag);
                stmt.setInt(2, this.id);
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Error updating clan tag: " + e.getMessage());
            }
        });
    }

    public void setName(String newName) {
        String oldName = this.name.toLowerCase();
        this.name = newName;
        plugin.getClanManager().getClans().remove(oldName);
        plugin.getClanManager().getClans().put(newName.toLowerCase(), this);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                PreparedStatement stmt = plugin.getConnection().prepareStatement(
                        "UPDATE clans SET name = ? WHERE id = ?"
                );
                stmt.setString(1, newName);
                stmt.setInt(2, this.id);
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Error updating clan name: " + e.getMessage());
            }
        });
    }



    public static class ClanInvite {
        private final UUID inviter;
        private final UUID invited;
        private final LocalDateTime timestamp;
        private final long EXPIRE_MINUTES = 5;

        public ClanInvite(UUID inviter, UUID invited) {
            this.inviter = inviter;
            this.invited = invited;
            this.timestamp = LocalDateTime.now();
        }

        public boolean isExpired() {
            return LocalDateTime.now().isAfter(timestamp.plusMinutes(EXPIRE_MINUTES));
        }
    }

    // Core member management
    public void addMember(UUID player) {
        members.add(player);
        memberRanks.put(player, ClanRank.RECRUIT);
    }

    public void removeMember(UUID player) {
        members.remove(player);
        moderators.remove(player);
        memberRanks.remove(player);
    }

    public void promoteMember(UUID player) {
        ClanRank currentRank = memberRanks.get(player);
        if (currentRank == ClanRank.RECRUIT) {
            memberRanks.put(player, ClanRank.MEMBER);
        } else if (currentRank == ClanRank.MEMBER) {
            memberRanks.put(player, ClanRank.MODERATOR);
            moderators.add(player);
        }
    }

    public void demoteMember(UUID player) {
        ClanRank currentRank = memberRanks.get(player);
        if (currentRank == ClanRank.MODERATOR) {
            memberRanks.put(player, ClanRank.MEMBER);
            moderators.remove(player);
        } else if (currentRank == ClanRank.MEMBER) {
            memberRanks.put(player, ClanRank.RECRUIT);
        }
    }

    // Achievement system
    public boolean hasAchievement(String id) {
        return achievements.contains(id);
    }

    public void addAchievement(String id) {
        achievements.add(id);
    }

    // Utility methods
    public void broadcast(String message) {
        String prefix = ChatColor.GRAY + "[" + ChatColor.AQUA + name + ChatColor.GRAY + "] ";
        members.forEach(uuid -> {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.sendMessage(prefix + message);
            }
        });
    }

    public boolean isMember(UUID player) {
        return members.contains(player);
    }

    public boolean isModerator(UUID player) {
        return moderators.contains(player);
    }

    public boolean isLeader(UUID player) {
        return leader.equals(player);
    }

    // Getters and Setters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getTag() { return tag; }
    public UUID getLeader() { return leader; }
    public void setLeader(UUID leader) { this.leader = leader; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public int getExperience() { return experience; }
    public void setExperience(int experience) { this.experience = experience; }
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
    public Location getHome() { return home; }
    public void setHome(Location home) { this.home = home; }
    public int getMaxMembers() { return maxMembers; }
    public void setMaxMembers(int maxMembers) { this.maxMembers = maxMembers; }
    public int getMaxTerritories() { return maxTerritories; }
    public void setMaxTerritories(int maxTerritories) { this.maxTerritories = maxTerritories; }
    public double getBankLimit() { return bankLimit; }
    public void setBankLimit(double bankLimit) { this.bankLimit = bankLimit; }
    public Set<UUID> getMembers() { return members; }
    public Map<UUID, ClanRank> getMemberRanks() { return memberRanks; }
    public Map<UUID, ClanInvite> getPendingInvites() { return pendingInvites; }
    public ClanRank getMemberRank(UUID player) { return memberRanks.getOrDefault(player, null); }
}
