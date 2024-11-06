package nl.inferno.customSMP.managers;

import nl.inferno.customSMP.CustomSMP;
import nl.inferno.customSMP.enums.ClanPermission;
import nl.inferno.customSMP.enums.ClanRank;
import nl.inferno.customSMP.models.Clan;

import java.util.*;

public class PermissionManager {
    private final CustomSMP plugin;
    private final Map<ClanRank, Set<ClanPermission>> rankPermissions = new HashMap<>();
    private final Map<Integer, Map<UUID, Set<ClanPermission>>> customPermissions = new HashMap<>();

    public PermissionManager(CustomSMP plugin) {
        this.plugin = plugin;
        initializeDefaultPermissions();
    }

    private void initializeDefaultPermissions() {
        // LEADER permissions
        Set<ClanPermission> leaderPerms = EnumSet.allOf(ClanPermission.class);
        rankPermissions.put(ClanRank.LEADER, leaderPerms);

        // MODERATOR permissions
        Set<ClanPermission> modPerms = EnumSet.of(
                ClanPermission.INVITE_MEMBERS,
                ClanPermission.KICK_MEMBERS,
                ClanPermission.SET_HOME,
                ClanPermission.USE_HOME,
                ClanPermission.WITHDRAW_MONEY,
                ClanPermission.VIEW_BANK,
                ClanPermission.MANAGE_RANKS
        );
        rankPermissions.put(ClanRank.MODERATOR, modPerms);

        // MEMBER permissions
        Set<ClanPermission> memberPerms = EnumSet.of(
                ClanPermission.USE_HOME,
                ClanPermission.VIEW_BANK
        );
        rankPermissions.put(ClanRank.MEMBER, memberPerms);

        // RECRUIT permissions
        Set<ClanPermission> recruitPerms = EnumSet.of(
                ClanPermission.USE_HOME
        );
        rankPermissions.put(ClanRank.RECRUIT, recruitPerms);
    }

    public boolean hasPermission(UUID player, ClanPermission permission) {
        Clan clan = plugin.getClanManager().getPlayerClan(player);
        if (clan == null) return false;

        // Check custom permissions first
        Map<UUID, Set<ClanPermission>> clanCustomPerms = customPermissions.get(clan.getId());
        if (clanCustomPerms != null && clanCustomPerms.containsKey(player)) {
            if (clanCustomPerms.get(player).contains(permission)) {
                return true;
            }
        }

        // Check rank permissions
        ClanRank rank = clan.getMemberRank(player);
        return rankPermissions.get(rank).contains(permission);
    }

    public void addCustomPermission(int clanId, UUID player, ClanPermission permission) {
        customPermissions.computeIfAbsent(clanId, k -> new HashMap<>())
                .computeIfAbsent(player, k -> EnumSet.noneOf(ClanPermission.class))
                .add(permission);
        saveCustomPermissions(clanId, player);
    }

    public void removeCustomPermission(int clanId, UUID player, ClanPermission permission) {
        Map<UUID, Set<ClanPermission>> clanCustomPerms = customPermissions.get(clanId);
        if (clanCustomPerms != null && clanCustomPerms.containsKey(player)) {
            clanCustomPerms.get(player).remove(permission);
            saveCustomPermissions(clanId, player);
        }
    }

    private void saveCustomPermissions(int clanId, UUID player) {
        // Save custom permissions to database
    }

    public Set<ClanPermission> getRankPermissions(ClanRank rank) {
        return new HashSet<>(rankPermissions.get(rank));
    }
}
