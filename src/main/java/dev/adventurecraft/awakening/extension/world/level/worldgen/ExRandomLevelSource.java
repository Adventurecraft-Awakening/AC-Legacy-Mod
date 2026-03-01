package dev.adventurecraft.awakening.extension.world.level.worldgen;

import net.minecraft.world.level.levelgen.RandomLevelSource;

public interface ExRandomLevelSource {

    void ac$initCopy();

    RandomLevelSource ac$clone();
}
