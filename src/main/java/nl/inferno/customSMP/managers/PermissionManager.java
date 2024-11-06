package nl.inferno.customSMP.managers;

import nl.inferno.customSMP.enums.ClanPermission;
import nl.inferno.customSMP.enums.ClanRank;
import nl.inferno.customSMP.models.Clan;

import java.util.*;

public class PermissionManager {
    private final Map<ClanRank, Set<ClanPermission>> rankPermissions = new HashMap<>();

    public PermissionManager() {
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
            ClanPermission.WITHDRAW_MONEY
        );
        rankPermissions.put(ClanRank.MODERATOR, modPerms);

        // MEMBER permissions
        Set<ClanPermission> memberPerms = EnumSet.of(
            ClanPermission.USE_HOME,
            ClanPermission.VIEW_BANK
        );
        rankPermissions.put(ClanRank.MEMBER, memberPerms);
    }

    public boolean hasPermission(UUID player, ClanPermission permission) {
        Clan clan = ClanManager.getInstance().getPlayerClan(player);
        if (clan == null) return false;

        ClanRank rank = clan.getMemberRank(player);
        return rankPermissions.get(rank).contains(permission);
    }
}
