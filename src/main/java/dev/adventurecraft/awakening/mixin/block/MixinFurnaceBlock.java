package dev.adventurecraft.awakening.mixin.block;

import dev.adventurecraft.awakening.extension.block.ExBlock;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.FurnaceTile;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FurnaceTile.class)
public abstract class MixinFurnaceBlock extends MixinBlock implements ExBlock {

    @Override
    public void ac$onRemove(Level level, int x, int y, int z, boolean dropItems) {
        if (dropItems) {
            super.onRemove(level, x, y, z);
        } else {
            level.removeTileEntity(x, y, z);
        }
    }
}
