package dev.adventurecraft.awakening.mixin.world.level.chunk.storage;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.extension.world.level.chunk.storage.ExRegionFileCache;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.chunk.storage.RegionFileCache;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

@Mixin(RegionFileCache.class)
public abstract class MixinRegionFileCache implements ExRegionFileCache {

    // Vanilla code deletes all cached files when reaching the threshold.
    // This mixin attempts to drop the oldest file only,
    // which permits a lower and more meaningful threshold.
    @Unique private static final int FILE_CACHE_SIZE = 128;

    @Shadow @Final private static Map<File, Reference<RegionFile>> cache;

    @Unique private static final Deque<File> order = new ArrayDeque<>();

    @Overwrite
    public static synchronized RegionFile getRegionFile(File basePath, int x, int z) {
        var regionDir = new File(basePath, "region");
        var regionFile = new File(regionDir, "r." + (x >> 5) + "." + (z >> 5) + ".mcr");
        Reference<RegionFile> ref = cache.get(regionFile);
        if (ref != null) {
            RegionFile region = ref.get();
            if (region != null) {
                return region;
            }
        }
        if (!regionDir.exists()) {
            regionDir.mkdirs();
        }

        if (order.size() >= FILE_CACHE_SIZE) {
            File oldFile = order.removeFirst();
            Reference<RegionFile> oldRef = cache.remove(oldFile);
            RegionFile oldRegion = oldRef.get();
            try {
                // TODO: push files onto save-queue instead of closing inside this method?
                closeRegion(oldRegion);
            }
            catch (IOException ioEx) {
                ACMod.LOGGER.warn("Failed to close region: ", ioEx);
            }
        }
        var region = new RegionFile(regionFile);
        cache.put(regionFile, new SoftReference<>(region));
        return region;
    }

    @Unique
    private static void closeRegion(@Nullable RegionFile region)
        throws IOException {
        if (region != null) {
            region.close();
        }
    }

    @Inject(
        method = "clear",
        at = @At("RETURN")
    )
    private static void clearOrder(CallbackInfo ci) {
        order.clear();
    }
}
