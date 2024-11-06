package nl.inferno.customSMP.systems;

import nl.inferno.customSMP.CustomSMP;
import nl.inferno.customSMP.models.Clan;
import nl.inferno.customSMP.utils.ChunkKey;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class TerritorySystem {
    private final CustomSMP plugin;
    private final Map<ChunkKey, Territory> territories = new HashMap<>();

    public TerritorySystem(CustomSMP plugin) {
        this.plugin = plugin;
        loadTerritories();
    }

    public int getClaimedChunksCount(Clan clan) {
        return territories.values().stream()
                .filter(territory -> territory.getOwner().equals(clan))
                .mapToInt(territory -> territory.getChunks().size())
                .sum();
    }


    public class Territory {
        private final Clan owner;
        private final Set<ChunkKey> chunks = new HashSet<>();
        private final TerritoryFlags flags = new TerritoryFlags();
        private final Set<UUID> trustedPlayers = new HashSet<>();

        public Territory(Clan owner) {
            this.owner = owner;
        }

        public boolean hasAccess(Player player) {
            return owner.isMember(player.getUniqueId()) || trustedPlayers.contains(player.getUniqueId());
        }

        public Object getOwner() {
            return owner;
        }

        public Map<Object, Object> getChunks() {
            return (Map<Object, Object>) chunks;
        }
    }

    public class TerritoryFlags {
        private boolean pvpEnabled = false;
        private boolean mobSpawning = true;
        private boolean explosions = false;
        private boolean fireSpread = false;
    }

    private void loadTerritories() {
        // Load territories from database
    }

    public void claimChunk(Clan clan, Chunk chunk) {
        ChunkKey key = new ChunkKey(chunk);
        if (!territories.containsKey(key)) {
            Territory territory = new Territory(clan);
            territory.chunks.add(key);
            territories.put(key, territory);
            saveTerritoryToDatabase(territory, key);
        }
    }

    public boolean canBuild(Player player, Location location) {
        Territory territory = territories.get(new ChunkKey(location.getChunk()));
        if (territory == null) return true;
        return territory.hasAccess(player);
    }

    private void saveTerritoryToDatabase(Territory territory, ChunkKey key) {
        // Save territory to database
    }
}
