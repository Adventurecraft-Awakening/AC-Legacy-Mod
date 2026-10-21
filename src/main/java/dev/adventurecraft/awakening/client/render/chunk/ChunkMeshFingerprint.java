package dev.adventurecraft.awakening.client.render.chunk;

import dev.adventurecraft.awakening.client.render.VertexFormat;

import java.nio.ByteBuffer;

/** Transport content identity, independent of allocation and legacy baked-light remeshing. */
public final class ChunkMeshFingerprint {
    public static final long SEED = 0xcbf29ce484222325L;
    private static final long PRIME = 0x100000001b3L;

    private ChunkMeshFingerprint() { }

    public static long append(long hash, Iterable<ByteBuffer> blocks, int meshIndex) {
        hash = (hash ^ meshIndex) * PRIME;
        for (var block : blocks) {
            // Held-light updates rebuild baked RGB/skylight without changing ray
            // transport. Unlit material tint and emission are stored in the tag.
            for (int at = block.position(); at < block.limit(); at += VertexFormat.STRIDE) {
                for (int word = 0; word < VertexFormat.COLOR_OFFSET; word += Integer.BYTES) {
                    hash = (hash ^ block.getInt(at + word)) * PRIME;
                }
                hash = (hash ^ (block.get(at + VertexFormat.COLOR_OFFSET + 3) & 0xff)) * PRIME;
                for (int component = 0; component < 3; component++) {
                    hash = (hash ^ (block.get(at + VertexFormat.NORMAL_OFFSET + component) & 0xff)) * PRIME;
                }
                // Preserve water/rain/transmission flags; omit baked skylight.
                hash = (hash ^ (block.get(at + VertexFormat.NORMAL_OFFSET + 3) & 0xe0)) * PRIME;
                hash = (hash ^ block.getInt(at + VertexFormat.MATERIAL_OFFSET)) * PRIME;
            }
        }
        return hash;
    }
}
