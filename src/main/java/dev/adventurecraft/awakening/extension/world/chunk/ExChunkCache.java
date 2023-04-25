package dev.adventurecraft.awakening.extension.world.chunk;

import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkIO;
import net.minecraft.world.source.WorldSource;

public interface ExChunkCache {

    void init(World var1, ChunkIO var2, WorldSource var3);

    void updateVeryFar();
}
