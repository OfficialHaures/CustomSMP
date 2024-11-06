package nl.inferno.customSMP.systems;

import nl.inferno.customSMP.CustomSMP;
import nl.inferno.customSMP.models.Clan;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;

public class LevelSystem {
    private final Map<Integer, Integer> experienceRequirements = new HashMap<>();
    private final CustomSMP plugin;
    private final Map<Integer, LevelReward> levelRewards = new HashMap<>();

    public LevelSystem(CustomSMP plugin) {
        this.plugin = plugin;
        initializeLevelRequirements();
        initializeLevelRewards();
    }

    private static class LevelReward {
        private final int maxMembers;
        private final int maxTerritories;
        private final double bankLimit;

        public LevelReward(int maxMembers, int maxTerritories, double bankLimit) {
            this.maxMembers = maxMembers;
            this.maxTerritories = maxTerritories;
            this.bankLimit = bankLimit;
        }
    }

    private void initializeLevelRequirements() {
        for (int level = 1; level <= 100; level++) {
            experienceRequirements.put(level, calculateRequiredExp(level));
        }
    }

    private int calculateRequiredExp(int level) {
        return (int)(1000 * Math.pow(1.5, level - 1));
    }

    private void initializeLevelRewards() {
        levelRewards.put(1, new LevelReward(10, 5, 10000));
        levelRewards.put(5, new LevelReward(15, 10, 50000));
        levelRewards.put(10, new LevelReward(20, 20, 100000));
        levelRewards.put(25, new LevelReward(30, 40, 500000));
        levelRewards.put(50, new LevelReward(50, 100, 1000000));
    }

    public void addExperience(Clan clan, int amount) {
        int currentExp = clan.getExperience() + amount;
        clan.setExperience(currentExp);

        updateExperienceInDatabase(clan);
        checkLevelUp(clan);

        clan.broadcast(ChatColor.GREEN + "+" + amount + " clan experience!");
    }

    private void updateExperienceInDatabase(Clan clan) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                PreparedStatement stmt = plugin.getConnection().prepareStatement(
                        "UPDATE clans SET experience = ? WHERE id = ?"
                );
                stmt.setInt(1, clan.getExperience());
                stmt.setInt(2, clan.getId());
                stmt.executeUpdate();
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to update clan experience: " + e.getMessage());
            }
        });
    }

    private void checkLevelUp(Clan clan) {
        int nextLevel = clan.getLevel() + 1;
        if (experienceRequirements.containsKey(nextLevel) &&
                clan.getExperience() >= experienceRequirements.get(nextLevel)) {
            levelUp(clan);
        }
    }

    private void levelUp(Clan clan) {
        int newLevel = clan.getLevel() + 1;
        clan.setLevel(newLevel);

        applyLevelRewards(clan, newLevel);
        updateLevelInDatabase(clan);
        announceLevel(clan, newLevel);
    }

    private void applyLevelRewards(Clan clan, int level) {
        LevelReward reward = levelRewards.get(level);
        if (reward != null) {
            clan.setMaxMembers(reward.maxMembers);
            clan.setMaxTerritories(reward.maxTerritories);
            clan.setBankLimit(reward.bankLimit);
        }
    }

    private void updateLevelInDatabase(Clan clan) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                PreparedStatement stmt = plugin.getConnection().prepareStatement(
                        "UPDATE clans SET level = ?, max_members = ? WHERE id = ?"
                );
                stmt.setInt(1, clan.getLevel());
                stmt.setInt(2, clan.getMaxMembers());
                stmt.setInt(3, clan.getId());
                stmt.executeUpdate();
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to update clan level: " + e.getMessage());
            }
        });
    }

    private void announceLevel(Clan clan, int newLevel) {
        String message = ChatColor.GOLD + "Level Up! " +
                ChatColor.YELLOW + "New level: " + newLevel + "\n" +
                ChatColor.GREEN + "New rewards unlocked!";
        clan.broadcast(message);
    }

    public int getRequiredExperience(int level) {
        return experienceRequirements.getOrDefault(level, Integer.MAX_VALUE);
    }
}
