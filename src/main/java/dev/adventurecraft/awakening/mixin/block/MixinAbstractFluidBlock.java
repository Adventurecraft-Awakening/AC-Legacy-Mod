package dev.adventurecraft.awakening.mixin.block;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.common.AC_TerrainImage;
import net.minecraft.block.AbstractFluidBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractFluidBlock.class)
public abstract class MixinAbstractFluidBlock extends Block {

    protected MixinAbstractFluidBlock(int i, Material arg) {
        super(i, arg);
    }

    public int getColorMultiplier(BlockView var1, int var2, int var3, int var4) {
        if (AC_TerrainImage.isWaterLoaded) {
            if (this.id == Block.FLOWING_WATER.id || this.id == Block.STILL_WATER.id) {
                return AC_TerrainImage.getWaterColor(var2, var4);
            }
        }
        return -1;
    }

    public boolean isCollidable(int var1, boolean var2) {
        return AC_DebugMode.active && AC_DebugMode.isFluidHittable || var2 && var1 == 0;
    }

    public boolean isCollidable() {
        return AC_DebugMode.active &&  AC_DebugMode.isFluidHittable;
    }
}
