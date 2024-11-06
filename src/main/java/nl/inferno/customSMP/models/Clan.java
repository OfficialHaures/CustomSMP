package nl.inferno.customSMP.models;

import nl.inferno.customSMP.enums.ClanRank;
import org.bukkit.Location;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Clan {
    private int id;
    private String name;
    private UUID leader;
    private String description;
    private String tag;
    private int maxMembers;
    private double balance;
    private Location home;
    private Set<UUID> members;
    private Set<UUID> moderators;
    private Map<UUID, ClanRank> memberRanks;
    private LocalDateTime createdAt;
    private int level;
    private int experience;


    public int getId() {
        return id;
    }

    public int getExperience() {
        return 0;
    }

    public ClanRank getMemberRank(UUID player) {
        return ClanRank.MEMBER;
    }
}
