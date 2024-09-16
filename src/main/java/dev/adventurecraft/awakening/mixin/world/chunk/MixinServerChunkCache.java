package dev.adventurecraft.awakening.mixin.world.chunk;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Set;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.levelgen.ServerChunkCache;

@Mixin(ServerChunkCache.class)
public abstract class MixinServerChunkCache {

    @Shadow
    private Set<Integer> dropSet;

    @Shadow
    private Map<Integer, LevelChunk> serverChunkCache;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(Level var1, ChunkStorage var2, ChunkSource var3, CallbackInfo ci) {
        this.dropSet = new IntOpenHashSet();
        this.serverChunkCache = new Int2ObjectOpenHashMap<>();
    }
}
