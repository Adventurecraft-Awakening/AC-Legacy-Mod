package dev.adventurecraft.awakening.mixin.world.level.tile;

import dev.adventurecraft.awakening.extension.world.level.tile.ExTileEntityTile;
import net.minecraft.world.level.tile.TileEntityTile;
import net.minecraft.world.level.tile.entity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TileEntityTile.class)
public abstract class MixinTileEntityTile implements ExTileEntityTile {

    @Shadow
    protected abstract TileEntity newTileEntity();

    public @Override TileEntity ac$newTileEntity() {
        return this.newTileEntity();
    }
}
