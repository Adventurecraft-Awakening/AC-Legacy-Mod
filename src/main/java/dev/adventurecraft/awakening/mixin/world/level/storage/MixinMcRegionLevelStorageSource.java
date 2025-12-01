package dev.adventurecraft.awakening.mixin.world.level.storage;

import dev.adventurecraft.awakening.world.level.storage.AsyncMcRegionChunkStorage;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.dimension.Dimension;
import net.minecraft.world.level.dimension.HellDimension;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.McRegionLevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.io.File;

@Mixin(McRegionLevelStorageSource.class)
public abstract class MixinMcRegionLevelStorageSource extends LevelStorage {

    public MixinMcRegionLevelStorageSource(File savesFolder, String name, boolean savePlayers) {
        super(savesFolder, name, savePlayers);
    }

    @Overwrite
    public ChunkStorage readDimension(Dimension dimension) {
        File file = this.getFolder();
        if (dimension instanceof HellDimension) {
            File file2 = new File(file, "DIM-1");
            file2.mkdirs();
            return new AsyncMcRegionChunkStorage(file2);
        }
        return new AsyncMcRegionChunkStorage(file);
    }
}
