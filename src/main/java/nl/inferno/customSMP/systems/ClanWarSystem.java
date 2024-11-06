package nl.inferno.customSMP.systems;

import nl.inferno.customSMP.CustomSMP;
import nl.inferno.customSMP.models.Clan;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalDateTime;
import java.util.*;

public class ClanWarSystem {
    private final CustomSMP plugin;
    private final Map<String, ClanWar> activeWars = new HashMap<>();

    public ClanWarSystem(CustomSMP plugin) {
        this.plugin = plugin;
    }

    public class ClanWar {
        private final Clan challenger;
        private final Clan defender;
        private int challengerScore = 0;
        private int defenderScore = 0;
        private final LocalDateTime startTime;
        private LocalDateTime endTime;
        private final Set<UUID> participants = new HashSet<>();
        private boolean isActive = true;

        public ClanWar(Clan challenger, Clan defender) {
            this.challenger = challenger;
            this.defender = defender;
            this.startTime = LocalDateTime.now();
            this.endTime = startTime.plusHours(1);
            initializeParticipants();
        }

        private void initializeParticipants() {
            participants.addAll(challenger.getMembers());
            participants.addAll(defender.getMembers());
        }
    }

    public void startWar(Clan challenger, Clan defender) {
        String warId = getWarId(challenger, defender);
        ClanWar war = new ClanWar(challenger, defender);
        activeWars.put(warId, war);

        announceWar(war);
        startWarTimer(war);
    }

    private String getWarId(Clan clan1, Clan clan2) {
        return clan1.getId() + "_" + clan2.getId();
    }

    private void announceWar(ClanWar war) {
        String message = ChatColor.RED + "WAR STARTED: " +
                ChatColor.YELLOW + war.challenger.getName() +
                ChatColor.WHITE + " vs " +
                ChatColor.YELLOW + war.defender.getName();

        war.challenger.broadcast(message);
        war.defender.broadcast(message);
    }

    private void startWarTimer(ClanWar war) {
        new BukkitRunnable() {
            @Override
            public void run() {
                endWar(war);
            }
        }.runTaskLater(plugin, 20 * 60 * 60); // 1 hour
    }

    public void handleKill(Player killer, Player victim) {
        ClanWar war = getWarByPlayer(killer.getUniqueId());
        if (war != null && war.isActive) {
            updateWarScore(war, killer);
        }
    }

    private ClanWar getWarByPlayer(UUID playerId) {
        return activeWars.values().stream()
                .filter(war -> war.participants.contains(playerId))
                .findFirst()
                .orElse(null);
    }

    private void updateWarScore(ClanWar war, Player killer) {
        Clan killerClan = plugin.getClanManager().getPlayerClan(killer.getUniqueId());
        if (killerClan.equals(war.challenger)) {
            war.challengerScore++;
        } else {
            war.defenderScore++;
        }
        broadcastScore(war);
    }

    private void broadcastScore(ClanWar war) {
        String scoreMessage = ChatColor.GOLD + "War Score: " +
                ChatColor.YELLOW + war.challenger.getName() + ChatColor.WHITE + ": " + war.challengerScore +
                ChatColor.GRAY + " vs " +
                ChatColor.YELLOW + war.defender.getName() + ChatColor.WHITE + ": " + war.defenderScore;

        war.challenger.broadcast(scoreMessage);
        war.defender.broadcast(scoreMessage);
    }

    private void endWar(ClanWar war) {
        war.isActive = false;
        Clan winner = determineWinner(war);
        announceWinner(war, winner);
        grantRewards(winner);
        activeWars.remove(getWarId(war.challenger, war.defender));
    }

    private Clan determineWinner(ClanWar war) {
        return war.challengerScore > war.defenderScore ? war.challenger : war.defender;
    }

    private void announceWinner(ClanWar war, Clan winner) {
        String message = ChatColor.GOLD + "War Ended! Winner: " + ChatColor.YELLOW + winner.getName();
        war.challenger.broadcast(message);
        war.defender.broadcast(message);
    }

    private void grantRewards(Clan winner) {
        // Grant experience and other rewards
        plugin.getLevelSystem().addExperience(winner, 1000);
    }
}
