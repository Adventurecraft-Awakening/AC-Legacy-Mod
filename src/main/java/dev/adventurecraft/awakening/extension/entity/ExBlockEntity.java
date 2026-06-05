package dev.adventurecraft.awakening.extension.entity;

import dev.adventurecraft.awakening.world.BlockPos;

public interface ExBlockEntity {

    String getClassName();

    BlockPos getBlockPos();

    boolean isKilledFromSaving();

    void setKilledFromSaving(boolean value);
}
