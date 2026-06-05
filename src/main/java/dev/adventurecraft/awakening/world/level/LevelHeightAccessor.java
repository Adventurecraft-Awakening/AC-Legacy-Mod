package dev.adventurecraft.awakening.world.level;

public interface LevelHeightAccessor {

    int ac$getMinY();

    int ac$getMaxY();

    int ac$getBaseHeight(int x, int z);

    default int ac$getHeight() {
        return this.ac$getMaxY() - this.ac$getMinY() + 1;
    }
}