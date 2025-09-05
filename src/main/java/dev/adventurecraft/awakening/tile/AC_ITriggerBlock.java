package dev.adventurecraft.awakening.tile;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;

public interface AC_ITriggerBlock {

    default int getBlockLightValue(LevelSource view, int x, int y, int z) {
        return 0;
    }

    @Environment(EnvType.CLIENT)
    int getRenderShape();

    @Environment(EnvType.CLIENT)
    default int getRenderShape(LevelSource view, int x, int y, int z) {
        return this.getRenderShape();
    }

    default boolean canBeTriggered() {
        return false;
    }

    default void addTriggerActivation(Level world, int x, int y, int z) {
        if (this.canBeTriggered()) {
            int meta = Math.min(world.getData(x, y, z) + 1, 15);
            world.setDataNoUpdate(x, y, z, meta);
            if (meta == 1) {
                this.onTriggerActivated(world, x, y, z);
            }
        }
    }

    default void removeTriggerActivation(Level world, int x, int y, int z) {
        if (this.canBeTriggered()) {
            int meta = world.getData(x, y, z) - 1;
            world.setDataNoUpdate(x, y, z, Math.max(meta, 0));
            if (meta == 0) {
                this.onTriggerDeactivated(world, x, y, z);
            }
        }
    }

    default void onTriggerActivated(Level world, int x, int y, int z) {
    }

    default void onTriggerDeactivated(Level world, int x, int y, int z) {
    }

    default void reset(Level world, int x, int y, int z, boolean forDeath) {
    }

    default int alwaysUseClick(Level world, int x, int y, int z) {
        return -1;
    }
}
