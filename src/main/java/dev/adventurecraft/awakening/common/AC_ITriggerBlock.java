package dev.adventurecraft.awakening.common;

import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public interface AC_ITriggerBlock {

    default int getBlockLightValue(BlockView view, int x, int y, int z) {
        return 0;
    }

    default boolean shouldRender(BlockView view, int x, int y, int z) {
        return true;
    }

    default boolean canBeTriggered() {
        return false;
    }

    default void addTriggerActivation(World world, int x, int y, int z) {
        if (this.canBeTriggered()) {
            int meta = Math.min(world.getBlockMeta(x, y, z) + 1, 15);
            world.method_223(x, y, z, meta);
            if (meta == 1) {
                this.onTriggerActivated(world, x, y, z);
            }
        }
    }

    default void removeTriggerActivation(World world, int x, int y, int z) {
        if (this.canBeTriggered()) {
            int meta = world.getBlockMeta(x, y, z) - 1;
            world.method_223(x, y, z, Math.max(meta, 0));
            if (meta == 0) {
                this.onTriggerDeactivated(world, x, y, z);
            }
        }
    }

    default void onTriggerActivated(World world, int x, int y, int z) {
    }

    default void onTriggerDeactivated(World world, int x, int y, int z) {
    }

    default void reset(World world, int x, int y, int z, boolean forDeath) {
    }

    default int alwaysUseClick(World world, int x, int y, int z) {
        return -1;
    }
}
