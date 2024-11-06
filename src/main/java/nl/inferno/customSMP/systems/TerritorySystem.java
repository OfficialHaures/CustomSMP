package nl.inferno.customSMP.systems;

import nl.inferno.customSMP.models.Clan;
import nl.inferno.customSMP.utils.ChunkKey;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class TerritorySystem {
    private final Map<ChunkKey, Territory> territories = new HashMap<>();

    public class Territory {
        private Clan owner;
        private Set<ChunkKey> chunks;
        private TerritoryFlags flags;
        private Set<UUID> trustedPlayers;

        // Implementation
    }

    public void claimChunk(Clan clan, Chunk chunk) {
        ChunkKey key = new ChunkKey(chunk);
        if (!territories.containsKey(key)) {
            Territory territory = new Territory(clan);
            territories.put(key, territory);
        }
    }

    public boolean canBuild(Player player, Location location) {
        Territory territory = territories.get(new ChunkKey(location.getChunk()));
        if (territory == null) return true;

        return territory.hasAccess(player);
    }
}
