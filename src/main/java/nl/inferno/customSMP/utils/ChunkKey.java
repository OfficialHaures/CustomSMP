package nl.inferno.customSMP.utils;

import org.bukkit.Chunk;

import java.util.Objects;

public class ChunkKey {
    private final int x;
    private final int z;
    private final String world;

    public ChunkKey(Chunk chunk) {
        this.x = chunk.getX();
        this.z = chunk.getZ();
        this.world = chunk.getWorld().getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChunkKey chunkKey = (ChunkKey) o;
        return x == chunkKey.x && z == chunkKey.z && world.equals(chunkKey.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, z, world);
    }
}
