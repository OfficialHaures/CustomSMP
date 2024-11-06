package nl.inferno.customSMP.events;

import jdk.jfr.Event;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class ClanCreateEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player leader;
    private final String clanName;
    private boolean cancelled;

    public ClanCreateEvent(Player leader, String clanName) {
        this.leader = leader;
        this.clanName = clanName;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public void setCancelled(boolean b) {

    }

    // Getters and Cancellable implementation
}
