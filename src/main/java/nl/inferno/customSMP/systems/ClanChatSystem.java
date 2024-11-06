package nl.inferno.customSMP.systems;

import nl.inferno.customSMP.CustomSMP;
import nl.inferno.customSMP.managers.ClanManager;
import nl.inferno.customSMP.models.Clan;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ClanChatSystem implements Listener {
    private final Set<UUID> clanChatMode = new HashSet<>();
    private final CustomSMP plugin;
    private final ClanManager clanManager;

    public ClanChatSystem(CustomSMP plugin) {
        this.plugin = plugin;
        this.clanManager = plugin.getClanManager();
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (clanChatMode.contains(player.getUniqueId())) {
            event.setCancelled(true);
            Clan clan = clanManager.getPlayerClan(player.getUniqueId());
            if (clan != null) {
                String format = ChatColor.GRAY + "[" + ChatColor.AQUA + "Clan" + ChatColor.GRAY + "] " +
                        ChatColor.YELLOW + player.getName() + ChatColor.WHITE + ": " + event.getMessage();
                clan.broadcast(format);
            }
        }
    }

    public void toggleClanChat(Player player) {
        UUID uuid = player.getUniqueId();
        if (clanChatMode.contains(uuid)) {
            clanChatMode.remove(uuid);
            player.sendMessage(ChatColor.RED + "Clan chat uitgeschakeld");
        } else {
            clanChatMode.add(uuid);
            player.sendMessage(ChatColor.GREEN + "Clan chat ingeschakeld");
        }
    }
}
