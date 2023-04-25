package dev.adventurecraft.awakening.common;

import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkIO;

public class MapChunkLoader implements ChunkIO {
    private final ChunkIO mapRegion;
    private final ChunkIO saveRegion;

    public MapChunkLoader(ChunkIO var1, ChunkIO var2) {
        this.mapRegion = var1;
        this.saveRegion = var2;
    }

    public Chunk getChunk(World var1, int var2, int var3) {
        Chunk var4 = this.saveRegion.getChunk(var1, var2, var3);
        if (var4 == null) {
            var4 = this.mapRegion.getChunk(var1, var2, var3);
        }
        return var4;
    }

    public void saveChunk(World var1, Chunk var2) {
        this.saveRegion.saveChunk(var1, var2);
        if (AC_DebugMode.levelEditing) {
            this.mapRegion.saveChunk(var1, var2);
        }
    }

    public void iDoNothingToo(World var1, Chunk var2) {
    }

    public void method_810() {
    }

    public void iAmActuallyUseless() {
    }
}
