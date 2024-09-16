package dev.adventurecraft.awakening.mixin.world.chunk;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.extension.world.chunk.ExChunkCache;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ProgressListener;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkCache;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChunkCache.class)
public abstract class MixinChunkCache implements ExChunkCache {

    @Shadow
    int chunkX;

    @Shadow
    int chunkZ;

    @Shadow
    private LevelChunk field_1511;

    @Shadow
    private Level world;

    @Shadow
    private ChunkStorage field_1513;

    @Shadow
    private ChunkSource worldSource;

    @Shadow
    private LevelChunk[] chunks;

    boolean isVeryFar;
    int mask;
    int chunksWide;

    @Shadow
    public abstract boolean saveChunks(boolean bl, ProgressListener arg);

    @Shadow
    public abstract boolean method_1243(int i, int j);

    @Override
    public void init(Level var1, ChunkStorage var2, ChunkSource var3) {
        this.isVeryFar = Minecraft.instance.options.viewDistance != 0;
        this.updateVeryFar();
        this.chunkX = -999999999;
        this.chunkZ = -999999999;
        this.field_1511 = new EmptyLevelChunk(var1, new byte[-Short.MIN_VALUE], 0, 0);
        this.world = var1;
        this.field_1513 = var2;
        this.worldSource = var3;
    }

    @Override
    public void updateVeryFar() {
        boolean var1 = Minecraft.instance.options.viewDistance == 0;
        if (this.isVeryFar == var1) {
            return;
        }

        this.isVeryFar = var1;
        this.chunkX = -999999999;
        this.chunkZ = -999999999;
        if (this.chunks != null) {
            this.saveChunks(true, null);
        }

        LevelChunk[] var2 = this.chunks;
        if (this.isVeryFar) {
            this.chunks = new LevelChunk[4096];
            this.mask = 63;
            this.chunksWide = 64;
        } else {
            this.chunks = new LevelChunk[1024];
            this.mask = 31;
            this.chunksWide = 32;
        }

        if (var2 != null) {
            for (LevelChunk var4 : var2) {
                if (var4 != null && this.method_1243(var4.x, var4.z)) {
                    int var5 = var4.x & this.mask;
                    int var6 = var4.z & this.mask;
                    int var7 = var5 + var6 * this.chunksWide;
                    this.chunks[var7] = var4;
                }
            }
        }
    }

    @ModifyConstant(method = "method_1243", constant = @Constant(intValue = 15))
    private int useMask0(int value) {
        return this.mask;
    }

    @ModifyConstant(method = {"isChunkLoaded", "getChunk"}, constant = @Constant(intValue = 31))
    private int useMask1(int value) {
        return this.mask;
    }

    @ModifyConstant(method = {"isChunkLoaded", "getChunk"}, constant = @Constant(intValue = 32))
    private int useChunksWide0(int value) {
        return this.chunksWide;
    }

    @Redirect(
        method = "decorate",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/source/WorldSource;decorate(Lnet/minecraft/world/source/WorldSource;II)V"))
    private void setChunkPopulatingOnDecorate(ChunkSource instance, ChunkSource worldSource, int x, int z) {
        ACMod.chunkIsNotPopulating = false;
        instance.postProcess(worldSource, x, z);
        ACMod.chunkIsNotPopulating = true;
    }
}
