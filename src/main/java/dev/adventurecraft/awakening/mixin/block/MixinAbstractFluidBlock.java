package dev.adventurecraft.awakening.mixin.block;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.common.AC_TerrainImage;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.LiquidTile;
import net.minecraft.world.level.tile.Tile;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LiquidTile.class)
public abstract class MixinAbstractFluidBlock extends Tile {

    protected MixinAbstractFluidBlock(int i, Material arg) {
        super(i, arg);
    }

    public int getFoliageColor(LevelSource var1, int var2, int var3, int var4) {
        if (AC_TerrainImage.isWaterLoaded) {
            if (this.id == Tile.FLOWING_WATER.id || this.id == Tile.WATER.id) {
                return AC_TerrainImage.getWaterColor(var2, var4);
            }
        }
        return -1;
    }

    public boolean mayPick(int var1, boolean var2) {
        return AC_DebugMode.active && AC_DebugMode.isFluidHittable || var2 && var1 == 0;
    }

    public boolean mayPick() {
        return AC_DebugMode.active &&  AC_DebugMode.isFluidHittable;
    }
}
