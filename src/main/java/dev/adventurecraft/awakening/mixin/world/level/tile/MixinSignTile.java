package dev.adventurecraft.awakening.mixin.world.level.tile;

import dev.adventurecraft.awakening.tile.AC_BlockShapes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.SignTile;
import net.minecraft.world.level.tile.TileEntityTile;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SignTile.class)
public abstract class MixinSignTile extends TileEntityTile {

    protected MixinSignTile(int i, Material material) {
        super(i, material);
    }

    @Environment(value = EnvType.CLIENT)
    public @Override int getRenderShape() {
        return AC_BlockShapes.BLOCK_SIGN;
    }
}
