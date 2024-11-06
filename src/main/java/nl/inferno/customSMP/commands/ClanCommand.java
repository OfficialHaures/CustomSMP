package nl.inferno.customSMP.commands;

import nl.inferno.customSMP.CustomSMP;
import nl.inferno.customSMP.enums.ClanPermission;
import nl.inferno.customSMP.gui.ClanGUI;
import nl.inferno.customSMP.managers.ClanManager;
import nl.inferno.customSMP.models.Clan;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ClanCommand implements CommandExecutor, TabCompleter {
    private final CustomSMP plugin;
    private final ClanManager clanManager;

    public ClanCommand(CustomSMP plugin) {
        this.plugin = plugin;
        this.clanManager = plugin.getClanManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Dit commando kan alleen door spelers gebruikt worden!");
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
            case "kick" -> handleKick(player, args);
            case "promote" -> handlePromote(player, args);
            case "demote" -> handleDemote(player, args);
            case "sethome" -> handleSetHome(player);
            case "home" -> handleHome(player);
            case "bank" -> handleBank(player, args);
            case "chat" -> handleChat(player);
            case "war" -> handleWar(player, args);
            case "info" -> handleInfo(player, args);
            case "disband" -> handleDisband(player);
            case "rename" -> handleRename(player, args);
            case "settag" -> handleSetTag(player, args);
            default -> sendHelp(player);
        }

        return true;
    }

    private void handleRename(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Gebruik: /clan rename <nieuwe naam>");
            return;
        }

        Clan clan = clanManager.getPlayerClan(player.getUniqueId());
        if (clan == null || !clan.isLeader(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Je moet de clan leider zijn om dit te doen!");
            return;
        }

        String newName = args[1];
        if (newName.length() > 16) {
            player.sendMessage(ChatColor.RED + "Clan naam mag niet langer zijn dan 16 karakters!");
            return;
        }

        if (clanManager.getClanByName(newName) != null) {
            player.sendMessage(ChatColor.RED + "Deze clan naam bestaat al!");
            return;
        }

        clan.setName(newName);
        clan.broadcast(ChatColor.GREEN + "Clan naam is veranderd naar: " + newName);
    }

    private void handleSetTag(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Gebruik: /clan settag <nieuwe tag>");
            return;
        }

        Clan clan = clanManager.getPlayerClan(player.getUniqueId());
        if (clan == null || !clan.isLeader(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Je moet de clan leider zijn om dit te doen!");
            return;
        }

        String newTag = args[1];
        if (newTag.length() > 5) {
            player.sendMessage(ChatColor.RED + "Clan tag mag niet langer zijn dan 5 karakters!");
            return;
        }

        clan.setTag(newTag);
        clan.broadcast(ChatColor.GREEN + "Clan tag is veranderd naar: " + newTag);
    }

    private void handleCreate(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Gebruik: /clan create <naam> <tag>");
            return;
        }

        String clanName = args[1];
        String tag = args[2];

        if (clanManager.getPlayerClan(player.getUniqueId()) != null) {
            player.sendMessage(ChatColor.RED + "Je zit al in een clan!");
            return;
        }

        if (clanName.length() > 16) {
            player.sendMessage(ChatColor.RED + "Clan naam mag niet langer zijn dan 16 karakters!");
            return;
        }

        if (tag.length() > 5) {
            player.sendMessage(ChatColor.RED + "Clan tag mag niet langer zijn dan 5 karakters!");
            return;
        }

        clanManager.createClan(player, clanName, tag)
                .thenAccept(success -> {
                    if (success) {
                        player.sendMessage(ChatColor.GREEN + "Clan succesvol aangemaakt!");
                    } else {
                        player.sendMessage(ChatColor.RED + "Er ging iets mis bij het aanmaken van de clan.");
                    }
                });
    }

    private void handleDisband(Player player) {
        Clan clan = clanManager.getPlayerClan(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(ChatColor.RED + "Je zit niet in een clan!");
            return;
        }

        if (!clan.isLeader(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Alleen de leider kan de clan opheffen!");
            return;
        }

        clanManager.disbandClan(clan).thenAccept(success -> {
            if (success) {
                player.sendMessage(ChatColor.GREEN + "Clan succesvol opgeheven!");
            } else {
                player.sendMessage(ChatColor.RED + "Er ging iets mis bij het opheffen van de clan.");
            }
        });
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> commands = Arrays.asList(
                    "create", "invite", "join", "leave", "kick", "promote", "demote",
                    "sethome", "home", "bank", "chat", "war", "info", "disband",
                    "rename", "settag"
            );
            String input = args[0].toLowerCase();
            completions.addAll(commands.stream()
                    .filter(cmd -> cmd.startsWith(input))
                    .collect(Collectors.toList()));
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "invite", "kick", "promote", "demote" -> {
                    String input = args[1].toLowerCase();
                    completions.addAll(Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .filter(name -> name.toLowerCase().startsWith(input))
                            .collect(Collectors.toList()));
                }
                case "bank" -> {
                    String input = args[1].toLowerCase();
                    completions.addAll(Arrays.asList("deposit", "withdraw").stream()
                            .filter(op -> op.startsWith(input))
                            .collect(Collectors.toList()));
                }
            }
        }

        return completions;
    }

    private void openClanMenu(Player player) {
        new ClanGUI(player).open();
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Clan Commands ===");
        player.sendMessage(ChatColor.YELLOW + "/clan create <naam> <tag>" + ChatColor.GRAY + " - Maak een nieuwe clan");
        player.sendMessage(ChatColor.YELLOW + "/clan invite <speler>" + ChatColor.GRAY + " - Nodig een speler uit");
        player.sendMessage(ChatColor.YELLOW + "/clan join <clannaam>" + ChatColor.GRAY + " - Sluit je aan bij een clan");
        player.sendMessage(ChatColor.YELLOW + "/clan leave" + ChatColor.GRAY + " - Verlaat je clan");
        player.sendMessage(ChatColor.YELLOW + "/clan kick <speler>" + ChatColor.GRAY + " - Verwijder een clanlid");
        player.sendMessage(ChatColor.YELLOW + "/clan promote <speler>" + ChatColor.GRAY + " - Verhoog een clanlid in rank");
        player.sendMessage(ChatColor.YELLOW + "/clan demote <speler>" + ChatColor.GRAY + " - Verlaag een clanlid in rank");
        player.sendMessage(ChatColor.YELLOW + "/clan sethome" + ChatColor.GRAY + " - Stel een clanhuis in");
        player.sendMessage(ChatColor.YELLOW + "/clan home" + ChatColor.GRAY + " - Ga naar je clanhuis");
        player.sendMessage(ChatColor.YELLOW + "/clan bank" + ChatColor.GRAY + " - Bekijk de clanbank");
        player.sendMessage(ChatColor.YELLOW + "/clan chat" + ChatColor.GRAY + " - Toggle clan chat");
        player.sendMessage(ChatColor.YELLOW + "/clan war" + ChatColor.GRAY + " - Start een clan oorlog");
        player.sendMessage(ChatColor.YELLOW + "/clan info" + ChatColor.GRAY + " - Bekijk clan informatie");
        player.sendMessage(ChatColor.YELLOW + "/clan disband" + ChatColor.GRAY + " - Hef je clan op");
    }
    private void handleInvite(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Gebruik: /clan invite <speler>");
            return;
        }

        Clan clan = clanManager.getPlayerClan(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(ChatColor.RED + "Je zit niet in een clan!");
            return;
        }

        if (!plugin.getPermissionManager().hasPermission(player.getUniqueId(), ClanPermission.INVITE_MEMBERS)) {
            player.sendMessage(ChatColor.RED + "Je hebt geen permissie om leden uit te nodigen!");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Speler niet gevonden!");
            return;
        }

        clanManager.invitePlayer(clan, player, target);
    }

    private void handleJoin(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Gebruik: /clan join <clan naam>");
            return;
        }

        if (clanManager.getPlayerClan(player.getUniqueId()) != null) {
            player.sendMessage(ChatColor.RED + "Je zit al in een clan!");
            return;
        }

        String clanName = args[1];
        clanManager.acceptInvite(player, clanName);
    }

    private void handleLeave(Player player) {
        Clan clan = clanManager.getPlayerClan(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(ChatColor.RED + "Je zit niet in een clan!");
            return;
        }

        if (clan.isLeader(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Als leider moet je eerst de clan overdragen of opheffen!");
            return;
        }

        clanManager.removeMember(clan, player.getUniqueId());
        player.sendMessage(ChatColor.GREEN + "Je hebt de clan verlaten.");
        clan.broadcast(ChatColor.YELLOW + player.getName() + ChatColor.RED + " heeft de clan verlaten.");
    }

    private void handleKick(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Gebruik: /clan kick <speler>");
            return;
        }

        Clan clan = clanManager.getPlayerClan(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(ChatColor.RED + "Je zit niet in een clan!");
            return;
        }

        if (!plugin.getPermissionManager().hasPermission(player.getUniqueId(), ClanPermission.KICK_MEMBERS)) {
            player.sendMessage(ChatColor.RED + "Je hebt geen permissie om leden te kicken!");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Speler niet gevonden!");
            return;
        }

        if (target.getUniqueId().equals(clan.getLeader())) {
            player.sendMessage(ChatColor.RED + "Je kunt de leider niet kicken!");
            return;
        }

        if (!clan.isMember(target.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Deze speler zit niet in je clan!");
            return;
        }

        clanManager.removeMember(clan, target.getUniqueId());
        clan.broadcast(ChatColor.RED + target.getName() + " is gekickt uit de clan door " + player.getName());
    }

    private void handlePromote(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Gebruik: /clan promote <speler>");
            return;
        }

        Clan clan = clanManager.getPlayerClan(player.getUniqueId());
        if (clan == null || !clan.isLeader(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Je moet de clan leider zijn om dit te doen!");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Speler niet gevonden!");
            return;
        }

        if (!clan.isMember(target.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Deze speler zit niet in je clan!");
            return;
        }

        clanManager.promoteMember(clan, target.getUniqueId());
        clan.broadcast(ChatColor.GREEN + target.getName() + " is gepromoveerd naar " +
                clan.getMemberRank(target.getUniqueId()).name() + "!");
    }

    private void handleDemote(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Gebruik: /clan demote <speler>");
            return;
        }

        Clan clan = clanManager.getPlayerClan(player.getUniqueId());
        if (clan == null || !clan.isLeader(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Je moet de clan leider zijn om dit te doen!");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Speler niet gevonden!");
            return;
        }

        if (!clan.isMember(target.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Deze speler zit niet in je clan!");
            return;
        }

        clanManager.demoteMember(clan, target.getUniqueId());
        clan.broadcast(ChatColor.YELLOW + target.getName() + " is gedegradeerd naar " +
                clan.getMemberRank(target.getUniqueId()).name() + ".");
    }

    private void handleSetHome(Player player) {
        Clan clan = clanManager.getPlayerClan(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(ChatColor.RED + "Je zit niet in een clan!");
            return;
        }

        if (!plugin.getPermissionManager().hasPermission(player.getUniqueId(), ClanPermission.SET_HOME)) {
            player.sendMessage(ChatColor.RED + "Je hebt geen permissie om de clan home te zetten!");
            return;
        }

        clan.setHome(player.getLocation());
        clanManager.updateClanHome(clan);
        clan.broadcast(ChatColor.GREEN + player.getName() + " heeft de clan home verplaatst!");
    }

    private void handleHome(Player player) {
        Clan clan = clanManager.getPlayerClan(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(ChatColor.RED + "Je zit niet in een clan!");
            return;
        }

        if (!plugin.getPermissionManager().hasPermission(player.getUniqueId(), ClanPermission.USE_HOME)) {
            player.sendMessage(ChatColor.RED + "Je hebt geen permissie om de clan home te gebruiken!");
            return;
        }

        if (clan.getHome() == null) {
            player.sendMessage(ChatColor.RED + "Er is nog geen clan home gezet!");
            return;
        }

        player.teleport(clan.getHome());
        player.sendMessage(ChatColor.GREEN + "Geteleporteerd naar de clan home!");
    }

    private void handleBank(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Gebruik: /clan bank <deposit/withdraw> <bedrag>");
            return;
        }

        Clan clan = clanManager.getPlayerClan(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(ChatColor.RED + "Je zit niet in een clan!");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Ongeldig bedrag!");
            return;
        }

        switch (args[1].toLowerCase()) {
            case "deposit" -> plugin.getClanBankSystem().deposit(clan, player, amount);
            case "withdraw" -> {
                if (!plugin.getPermissionManager().hasPermission(player.getUniqueId(), ClanPermission.WITHDRAW_MONEY)) {
                    player.sendMessage(ChatColor.RED + "Je hebt geen permissie om geld op te nemen!");
                    return;
                }
                plugin.getClanBankSystem().withdraw(clan, player, amount);
            }
            default -> player.sendMessage(ChatColor.RED + "Gebruik: /clan bank <deposit/withdraw> <bedrag>");
        }
    }

    private void handleChat(Player player) {
        plugin.getClanChatSystem().toggleClanChat(player);
    }

    private void handleWar(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Gebruik: /clan war <clan naam>");
            return;
        }

        Clan attackerClan = clanManager.getPlayerClan(player.getUniqueId());
        if (attackerClan == null || !attackerClan.isLeader(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Je moet een clan leider zijn om oorlog te verklaren!");
            return;
        }

        Clan targetClan = clanManager.getClanByName(args[1]);
        if (targetClan == null) {
            player.sendMessage(ChatColor.RED + "Clan niet gevonden!");
            return;
        }

        plugin.getClanWarSystem().startWar(attackerClan, targetClan);
    }

    private void handleInfo(Player player, String[] args) {
        Clan clan;
        if (args.length > 1) {
            clan = clanManager.getClanByName(args[1]);
            if (clan == null) {
                player.sendMessage(ChatColor.RED + "Clan niet gevonden!");
                return;
            }
        } else {
            clan = clanManager.getPlayerClan(player.getUniqueId());
            if (clan == null) {
                player.sendMessage(ChatColor.RED + "Je zit niet in een clan!");
                return;
            }
        }

        sendClanInfo(player, clan);
    }

    private void sendClanInfo(Player player, Clan clan) {
        player.sendMessage(ChatColor.GOLD + "=== Clan Info ===");
        player.sendMessage(ChatColor.YELLOW + "Naam: " + ChatColor.WHITE + clan.getName());
        player.sendMessage(ChatColor.YELLOW + "Tag: " + ChatColor.WHITE + clan.getTag());
        player.sendMessage(ChatColor.YELLOW + "Level: " + ChatColor.WHITE + clan.getLevel());
        player.sendMessage(ChatColor.YELLOW + "Experience: " + ChatColor.WHITE + clan.getExperience() + "/" +
                plugin.getLevelSystem().getRequiredExperience(clan.getLevel() + 1));
        player.sendMessage(ChatColor.YELLOW + "Leider: " + ChatColor.WHITE +
                Bukkit.getOfflinePlayer(clan.getLeader()).getName());
        player.sendMessage(ChatColor.YELLOW + "Leden: " + ChatColor.WHITE +
                clan.getMembers().size() + "/" + clan.getMaxMembers());
        player.sendMessage(ChatColor.YELLOW + "Bank: " + ChatColor.WHITE + clan.getBalance());
    }

}
