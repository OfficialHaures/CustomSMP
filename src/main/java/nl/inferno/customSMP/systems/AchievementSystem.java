package nl.inferno.customSMP.systems;

import nl.inferno.customSMP.CustomSMP;
import nl.inferno.customSMP.models.Clan;
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.Map;

public class AchievementSystem {
    private final CustomSMP plugin;
    private final Map<String, ClanAchievement> achievements = new HashMap<>();

    public AchievementSystem(CustomSMP plugin) {
        this.plugin = plugin;
        registerAchievements();
    }

    private void registerAchievements() {
        addAchievement(new ClanAchievement(
                "first_level",
                "First Level Up!",
                "Reach clan level 2",
                500,
                clan -> clan.getLevel() >= 2
        ));

        addAchievement(new ClanAchievement(
                "wealthy_clan",
                "Wealthy Clan",
                "Accumulate 100,000 in clan bank",
                1000,
                clan -> clan.getBalance() >= 100000
        ));
    }

    private static class ClanAchievement {
        private final String id;
        private final String name;
        private final String description;
        private final int experienceReward;
        private final AchievementCondition condition;

        public ClanAchievement(String id, String name, String description,
                               int experienceReward, AchievementCondition condition) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.experienceReward = experienceReward;
            this.condition = condition;
        }
    }

    @FunctionalInterface
    private interface AchievementCondition {
        boolean check(Clan clan);
    }

    private void addAchievement(ClanAchievement achievement) {
        achievements.put(achievement.id, achievement);
    }

    public void checkAchievements(Clan clan) {
        for (ClanAchievement achievement : achievements.values()) {
            if (!clan.hasAchievement(achievement.id) && achievement.condition.check(clan)) {
                grantAchievement(clan, achievement);
            }
        }
    }

    private void grantAchievement(Clan clan, ClanAchievement achievement) {
        clan.addAchievement(achievement.id);
        plugin.getLevelSystem().addExperience(clan, achievement.experienceReward);
        clan.broadcast(ChatColor.GOLD + "Achievement Unlocked: " + ChatColor.YELLOW + achievement.name);
    }
}
