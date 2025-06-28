package dev.adventurecraft.awakening.mixin.world.level.chunk.storage;

import dev.adventurecraft.awakening.util.BufferOutputStream;
import net.minecraft.world.level.chunk.storage.RegionFile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.DataOutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

@Mixin(RegionFile.class)
public abstract class MixinRegionFile {

    @Shadow
    protected abstract boolean outOfBounds(int x, int z);

    public @Overwrite DataOutputStream open(int x, int z) {
        if (this.outOfBounds(x, z)) {
            return null;
        }
        var out = ((RegionFile) (Object) this).new ChunkBuffer(x, z);
        var deflater = new Deflater(); // TODO: use faster compression level based saves vs. mapedit?
        var deflaterOut = new DeflaterOutputStream(out, deflater, 512);
        return new DataOutputStream(new BufferOutputStream(deflaterOut, 1024));
    }
}
