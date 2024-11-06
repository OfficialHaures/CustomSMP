package nl.inferno.customSMP.systems;

import nl.inferno.customSMP.CustomSMP;
import nl.inferno.customSMP.models.Clan;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.Map;

public class LevelSystem {
    private final Map<Integer, Integer> experienceRequirements = new HashMap<>();
    private final CustomSMP plugin;

    public LevelSystem() {
        this.plugin = CustomSMP.getInstance();
        initializeLevelRequirements();
    }

    private void initializeLevelRequirements() {
        for (int level = 1; level <= 100; level++) {
            experienceRequirements.put(level, (int)(1000 * Math.pow(1.5, level - 1)));
        }
    }

    public void addExperience(Clan clan, int amount) {
        int currentExp = clan.getExperience() + amount;
        clan.setExperience(currentExp);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                plugin.getConnection().prepareStatement(
                    "UPDATE clans SET experience = " + currentExp + " WHERE id = " + clan.getId()
                ).executeUpdate();
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to update clan experience: " + e.getMessage());
            }
        });

        checkLevelUp(clan);
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
        clan.setMaxMembers(clan.getMaxMembers() + 2);

        // Update database
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                plugin.getConnection().prepareStatement(
                    "UPDATE clans SET level = " + newLevel + ", max_members = " + clan.getMaxMembers() +
                    " WHERE id = " + clan.getId()
                ).executeUpdate();
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to update clan level: " + e.getMessage());
            }
        });

        // Broadcast level up message to clan members
        clan.broadcast(ChatColor.GREEN + "Clan level up! " + ChatColor.YELLOW + "New level: " + newLevel);
    }

    public int getRequiredExperience(int level) {
        return experienceRequirements.getOrDefault(level, Integer.MAX_VALUE);
    }
}