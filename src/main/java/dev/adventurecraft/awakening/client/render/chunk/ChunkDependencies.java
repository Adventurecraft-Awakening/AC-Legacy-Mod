package dev.adventurecraft.awakening.client.render.chunk;

import dev.adventurecraft.awakening.extension.world.chunk.ExChunkCache;
import net.minecraft.client.renderer.Chunk;
import net.minecraft.world.level.Level;

/** Prepares only the world columns touched by a render section's padded snapshot. */
public final class ChunkDependencies {

    private static final int CHUNK_WIDTH = 16;

    private ChunkDependencies() {
    }

    public static boolean prepare(Level level, Chunk chunk) {
        Columns columns = columns(chunk.x, chunk.z, chunk.xs, chunk.zs);
        if (level.chunkSource instanceof ExChunkCache cache) {
            return columns.prepare(cache);
        }

        // Other chunk providers keep their existing synchronous loading behavior.
        for (int x = columns.minX; x <= columns.maxX; x++) {
            for (int z = columns.minZ; z <= columns.maxZ; z++) {
                level.getChunk(x, z);
            }
        }
        return true;
    }

    static Columns columns(int x, int z, int sizeX, int sizeZ) {
        int minX = Math.floorDiv(x - ChunkBuilder.PADDING.x, CHUNK_WIDTH);
        int minZ = Math.floorDiv(z - ChunkBuilder.PADDING.z, CHUNK_WIDTH);
        // Snapshot bounds are exclusive at the maximum, including at negative coordinates.
        int maxX = Math.floorDiv(x + sizeX + ChunkBuilder.PADDING.x - 1, CHUNK_WIDTH);
        int maxZ = Math.floorDiv(z + sizeZ + ChunkBuilder.PADDING.z - 1, CHUNK_WIDTH);
        return new Columns(minX, minZ, maxX, maxZ);
    }

    record Columns(int minX, int minZ, int maxX, int maxZ) {

        boolean prepare(ExChunkCache cache) {
            int missing = 0;
            int firstX = 0;
            int firstZ = 0;
            for (int x = this.minX; x <= this.maxX; x++) {
                for (int z = this.minZ; z <= this.maxZ; z++) {
                    // Request every exact dependency before publishing anything. Storage remains lazy
                    // for synchronous providers, and asynchronous providers queue each column once.
                    if (!cache.ac$prepareChunk(x, z, false)) {
                        if (missing == 0) {
                            firstX = x;
                            firstZ = z;
                        }
                        missing++;
                    }
                }
            }
            if (missing == 0) {
                return true;
            }

            // A generation/population call is indivisible. Let the scheduler check its frame budget
            // after at most one such unit, instead of generating a whole cold 3x3 footprint at once.
            boolean published = cache.ac$prepareChunk(firstX, firstZ, true);
            return missing == 1 && published;
        }
    }
}
