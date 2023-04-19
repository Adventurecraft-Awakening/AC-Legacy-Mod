package dev.adventurecraft.awakening.mixin.world.chunk;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkIO;
import net.minecraft.world.chunk.ServerChunkCache;
import net.minecraft.world.source.WorldSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Set;

@Mixin(ServerChunkCache.class)
public abstract class MixinServerChunkCache {

    @Shadow
    private Set<Integer> dropSet;

    @Shadow
    private Map<Integer, Chunk> serverChunkCache;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(World var1, ChunkIO var2, WorldSource var3, CallbackInfo ci) {
        this.dropSet = new IntOpenHashSet();
        this.serverChunkCache = new Int2ObjectOpenHashMap<>();
    }
}
