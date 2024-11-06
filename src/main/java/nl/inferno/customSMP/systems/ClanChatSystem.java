package nl.inferno.customSMP.systems;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ClanChatSystem implements Listener {
    private final Set<UUID> clanChatMode = new HashSet<>();

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (clanChatMode.contains(player.getUniqueId())) {
            event.setCancelled(true);
            sendClanMessage(player, event.getMessage());
        }
    }

    public void toggleClanChat(Player player) {
        if (clanChatMode.contains(player.getUniqueId())) {
            clanChatMode.remove(player.getUniqueId());
            player.sendMessage("Clan chat uitgeschakeld");
        } else {
            clanChatMode.add(player.getUniqueId());
            player.sendMessage("Clan chat ingeschakeld");
        }
    }
}
