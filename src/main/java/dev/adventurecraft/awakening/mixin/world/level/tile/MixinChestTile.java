package dev.adventurecraft.awakening.mixin.world.level.tile;

import dev.adventurecraft.awakening.mixin.block.MixinBlock;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.ChestTile;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChestTile.class)
public abstract class MixinChestTile extends MixinBlock {

    @Override
    public void ac$onRemove(Level level, int x, int y, int z, boolean dropItems) {
        if (dropItems) {
            super.onRemove(level, x, y, z);
        }
    }
}
