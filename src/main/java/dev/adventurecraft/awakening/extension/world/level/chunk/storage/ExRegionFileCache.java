package dev.adventurecraft.awakening.extension.world.level.chunk.storage;

import dev.adventurecraft.awakening.mixin.world.level.chunk.storage.AccessRegionFileCache;
import net.minecraft.world.level.chunk.storage.RegionFile;

import java.io.File;

public interface ExRegionFileCache {

    /**
     * Try to return an already open region handle from cache without opening new files.
     */
    static RegionFile tryGetRegionFile(File basePath, int x, int z) {
        File regionDir = new File(basePath, "region");
        File regionFile = new File(regionDir, "r." + (x >> 5) + "." + (z >> 5) + ".mcr");
        var reference = AccessRegionFileCache.getCache().get(regionFile);
        if (reference != null) {
            return reference.get();
        }
        return null;
    }
}
