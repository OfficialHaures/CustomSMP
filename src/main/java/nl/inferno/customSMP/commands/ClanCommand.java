package nl.inferno.customSMP.commands;

import nl.inferno.customSMP.CustomSMP;
import nl.inferno.customSMP.managers.ClanManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class ClanCommand implements CommandExecutor, TabCompleter {
    private final CustomSMP plugin;
    private final ClanManager clanManager;

    public ClanCommand(CustomSMP plugin) {
        this.plugin = plugin;
        this.clanManager = new ClanManager(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Dit commando kan alleen door spelers gebruikt worden!");
            return true;
        }

        if (args.length == 0) {
            openClanMenu(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create" -> handleCreate(player, args);
            case "invite" -> handleInvite(player, args);
            case "join" -> handleJoin(player, args);
            case "leave" -> handleLeave(player);
            case "sethome" -> handleSetHome(player);
            case "home" -> handleHome(player);
            case "bank" -> handleBank(player, args);
            default -> sendHelp(player);
        }

        return true;
    }

    private void handleCreate(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Gebruik: /clan create <naam> <tag>");
            return;
        }

        String clanName = args[1];
        String tag = args[2];

        clanManager.createClan(player, clanName, tag)
            .thenAccept(success -> {
                if (success) {
                    player.sendMessage(ChatColor.GREEN + "Clan succesvol aangemaakt!");
                } else {
                    player.sendMessage(ChatColor.RED + "Er ging iets mis bij het aanmaken van de clan.");
                }
            });
    }

    // Implementeer andere handlers...
}
