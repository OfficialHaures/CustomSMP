package nl.inferno.customSMP.systems;

import nl.inferno.customSMP.models.Clan;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClanBankSystem {
    private final Map<Integer, ClanBank> clanBanks = new HashMap<>();

    public class ClanBank {
        private double balance;
        private List<Transaction> transactions;
        private Map<UUID, Double> contributions;

        // Implementation
    }

    public void deposit(Clan clan, Player player, double amount) {
        ClanBank bank = clanBanks.get(clan.getId());
        bank.deposit(player.getUniqueId(), amount);
        logTransaction(clan, player, amount, TransactionType.DEPOSIT);
    }

    public boolean withdraw(Clan clan, Player player, double amount) {
        if (!hasPermission(player, ClanPermission.WITHDRAW_MONEY)) return false;

        ClanBank bank = clanBanks.get(clan.getId());
        return bank.withdraw(player.getUniqueId(), amount);
    }
}
