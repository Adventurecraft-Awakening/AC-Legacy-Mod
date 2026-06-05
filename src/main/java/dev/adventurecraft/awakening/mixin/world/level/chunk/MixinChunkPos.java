package dev.adventurecraft.awakening.mixin.world.level.chunk;

import dev.adventurecraft.awakening.extension.world.level.chunk.ExChunkPos;
import net.minecraft.world.level.chunk.ChunkPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ChunkPos.class)
public abstract class MixinChunkPos implements ExChunkPos {

    @Shadow @Final public int x;
    @Shadow @Final public int z;

    public @Override int x() {
        return this.x;
    }

    public @Override int z() {
        return this.z;
    }
}
