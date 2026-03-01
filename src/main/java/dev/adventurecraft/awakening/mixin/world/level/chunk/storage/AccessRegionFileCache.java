package dev.adventurecraft.awakening.mixin.world.level.chunk.storage;

import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.chunk.storage.RegionFileCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.io.File;
import java.lang.ref.Reference;
import java.util.Map;

@Mixin(RegionFileCache.class)
public interface AccessRegionFileCache {

    @Accessor
    static Map<File, Reference<RegionFile>> getCache() {
        throw new AssertionError();
    }
}
