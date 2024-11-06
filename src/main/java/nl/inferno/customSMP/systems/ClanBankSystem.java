package nl.inferno.customSMP.systems;

import nl.inferno.customSMP.CustomSMP;
import nl.inferno.customSMP.enums.ClanPermission;
import nl.inferno.customSMP.models.Clan;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.util.*;

public class ClanBankSystem {
    private final CustomSMP plugin;
    private final Map<Integer, ClanBank> clanBanks = new HashMap<>();

    public ClanBankSystem(CustomSMP plugin) {
        this.plugin = plugin;
        loadBanks();
    }

    private static class ClanBank {
        private double balance;
        private final List<Transaction> transactions = new ArrayList<>();
        private final Map<UUID, Double> contributions = new HashMap<>();

        public ClanBank(double initialBalance) {
            this.balance = initialBalance;
        }
    }

    private static class Transaction {
        private final UUID player;
        private final double amount;
        private final long timestamp;
        private final TransactionType type;

        public Transaction(UUID player, double amount, TransactionType type) {
            this.player = player;
            this.amount = amount;
            this.timestamp = System.currentTimeMillis();
            this.type = type;
        }
    }

    private enum TransactionType {
        DEPOSIT, WITHDRAW
    }

    private void loadBanks() {
        // Load banks from database
    }

    public void deposit(Clan clan, Player player, double amount) {
        ClanBank bank = getClanBank(clan);
        bank.balance += amount;
        bank.contributions.merge(player.getUniqueId(), amount, Double::sum);
        bank.transactions.add(new Transaction(player.getUniqueId(), amount, TransactionType.DEPOSIT));
        updateDatabase(clan, bank);
        player.sendMessage(ChatColor.GREEN + "Je hebt " + amount + " gestort in de clan bank!");
    }

    public boolean withdraw(Clan clan, Player player, double amount) {
        if (!CustomSMP.getInstance().getPermissionManager().hasPermission(player.getUniqueId(), ClanPermission.WITHDRAW_MONEY)) {
            player.sendMessage(ChatColor.RED + "Je hebt geen toestemming om geld op te nemen!");
            return false;
        }

        ClanBank bank = getClanBank(clan);
        if (bank.balance < amount) {
            player.sendMessage(ChatColor.RED + "Niet genoeg geld in de clan bank!");
            return false;
        }

        bank.balance -= amount;
        bank.transactions.add(new Transaction(player.getUniqueId(), amount, TransactionType.WITHDRAW));
        updateDatabase(clan, bank);
        player.sendMessage(ChatColor.GREEN + "Je hebt " + amount + " opgenomen uit de clan bank!");
        return true;
    }

    private ClanBank getClanBank(Clan clan) {
        return clanBanks.computeIfAbsent(clan.getId(), k -> new ClanBank(0));
    }

    private void updateDatabase(Clan clan, ClanBank bank) {
        // Update bank balance in database
    }
}
