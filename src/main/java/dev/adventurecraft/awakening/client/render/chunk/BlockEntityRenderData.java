package dev.adventurecraft.awakening.client.render.chunk;

import dev.adventurecraft.awakening.tile.entity.AC_TileEntityRedstoneTrigger;
import dev.adventurecraft.awakening.tile.entity.AC_TileEntityTree;
import net.minecraft.world.level.tile.entity.TileEntity;

import javax.annotation.Nullable;

/** The small immutable part of a block entity that terrain geometry actually uses. */
public sealed interface BlockEntityRenderData {
    record Tree(float size) implements BlockEntityRenderData { }
    record RedstoneTrigger(boolean activated) implements BlockEntityRenderData { }

    /** Called on the world thread; none of the resulting data retains a live entity. */
    static @Nullable BlockEntityRenderData capture(TileEntity entity) {
        return switch (entity) {
            case AC_TileEntityTree tree -> new Tree(tree.size);
            case AC_TileEntityRedstoneTrigger trigger -> new RedstoneTrigger(trigger.isActivated);
            default -> null;
        };
    }
}
