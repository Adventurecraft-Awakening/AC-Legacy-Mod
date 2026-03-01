package dev.adventurecraft.awakening.mixin.world.level.tile;

import net.minecraft.world.level.tile.RedstoneTorchTile;
import net.minecraft.world.level.tile.TorchTile;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(RedstoneTorchTile.class)
public abstract class MixinRedstoneTorchTile extends TorchTile {

    protected MixinRedstoneTorchTile(int id, int tex) {
        super(id, tex);
    }

    // Don't return redstone dust texture on side 1.
    @Override
    public int getTexture(int side, int meta) {
        return super.getTexture(side, meta);
    }
}
