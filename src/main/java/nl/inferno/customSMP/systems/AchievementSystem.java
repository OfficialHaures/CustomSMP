package nl.inferno.customSMP.systems;

import nl.inferno.customSMP.models.Clan;

import java.util.HashMap;
import java.util.Map;

public class AchievementSystem {
    private final Map<String, ClanAchievement> achievements = new HashMap<>();

    public class ClanAchievement {
        private String id;
        private String name;
        private String description;
        private int experienceReward;
        private Reward reward;

        // Implementation
    }

    public void checkAchievements(Clan clan) {
        for (ClanAchievement achievement : achievements.values()) {
            if (!clan.hasAchievement(achievement.getId()) && achievement.meetsRequirements(clan)) {
                grantAchievement(clan, achievement);
            }
        }
    }
}
