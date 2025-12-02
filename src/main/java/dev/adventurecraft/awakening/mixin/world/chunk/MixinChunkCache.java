package dev.adventurecraft.awakening.mixin.world.chunk;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.extension.world.chunk.ExChunkCache;
import dev.adventurecraft.awakening.world.level.storage.AsyncChunkSource;
import dev.adventurecraft.awakening.world.level.storage.AsyncChunkStorage;
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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChunkCache.class)
public abstract class MixinChunkCache implements ExChunkCache, AsyncChunkSource {

    @Shadow int xLast;
    @Shadow int zLast;

    @Shadow private LevelChunk emptyChunk;
    @Shadow private Level level;

    @Shadow private ChunkStorage storage;
    @Shadow private ChunkSource source;

    @Shadow private LevelChunk[] chunks;

    @Unique boolean isVeryFar;
    @Unique int mask;
    @Unique int chunksWide;

    @Shadow
    public abstract boolean save(boolean bl, ProgressListener arg);

    @Shadow
    public abstract boolean fits(int i, int j);

    @Shadow
    public abstract LevelChunk getChunk(int x, int z);

    @Override
    public void init(Level var1, ChunkStorage var2, ChunkSource var3) {
        this.isVeryFar = Minecraft.instance.options.viewDistance != 0;
        this.updateVeryFar();
        this.xLast = -999999999;
        this.zLast = -999999999;
        this.emptyChunk = new EmptyLevelChunk(var1, new byte[-Short.MIN_VALUE], 0, 0);
        this.level = var1;
        this.storage = var2;
        this.source = var3;
    }

    @Override
    public void updateVeryFar() {
        boolean var1 = Minecraft.instance.options.viewDistance == 0;
        if (this.isVeryFar == var1) {
            return;
        }

        this.isVeryFar = var1;
        this.xLast = -999999999;
        this.zLast = -999999999;
        if (this.chunks != null) {
            this.save(true, null);
        }

        LevelChunk[] var2 = this.chunks;
        if (this.isVeryFar) {
            this.chunks = new LevelChunk[4096];
            this.mask = 63;
            this.chunksWide = 64;
        }
        else {
            this.chunks = new LevelChunk[1024];
            this.mask = 31;
            this.chunksWide = 32;
        }

        if (var2 != null) {
            for (LevelChunk var4 : var2) {
                if (var4 != null && this.fits(var4.x, var4.z)) {
                    int var5 = var4.x & this.mask;
                    int var6 = var4.z & this.mask;
                    int var7 = var5 + var6 * this.chunksWide;
                    this.chunks[var7] = var4;
                }
            }
        }
    }

    @Override
    public int getCapacity() {
        return this.chunks.length;
    }

    @ModifyConstant(
        method = "fits",
        constant = @Constant(intValue = 15)
    )
    private int useMask0(int value) {
        return this.mask;
    }

    @ModifyConstant(
        method = {"hasChunk", "getChunk"},
        constant = @Constant(intValue = 31)
    )
    private int useMask1(int value) {
        return this.mask;
    }

    @ModifyConstant(
        method = {"hasChunk", "getChunk"},
        constant = @Constant(intValue = 32)
    )
    private int useChunksWide0(int value) {
        return this.chunksWide;
    }

    @Redirect(
        method = "postProcess",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/chunk/ChunkSource;postProcess(Lnet/minecraft/world/level/chunk/ChunkSource;II)V"
        )
    )
    private void setChunkPopulatingOnDecorate(ChunkSource instance, ChunkSource worldSource, int x, int z) {
        ACMod.chunkIsNotPopulating = false;
        instance.postProcess(worldSource, x, z);
        ACMod.chunkIsNotPopulating = true;
    }

    public void ac$requestChunks(int x0, int z0, int x1, int z1, boolean wait) {
        if (!(this.storage instanceof AsyncChunkStorage asyncStorage)) {
            return;
        }

        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                if (!this.level.isFindingSpawn && !this.fits(x, z)) {
                    continue;
                }
                if (this.hasChunk(x, z)) {
                    continue;
                }
                asyncStorage.requestAsync(this.level, x, z);
            }
        }

        if (wait) {
            for (int x = x0; x <= x1; x++) {
                for (int z = z0; z <= z1; z++) {
                    this.getChunk(x, z);
                }
            }
        }
    }
}