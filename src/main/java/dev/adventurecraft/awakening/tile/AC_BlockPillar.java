package dev.adventurecraft.awakening.tile;

import dev.adventurecraft.awakening.common.AC_IBlockColor;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.Tile;

public class AC_BlockPillar extends Tile implements AC_IBlockColor {

    public AC_BlockPillar(int var1, int var2) {
        super(var1, var2, Material.STONE);
    }

    @Override
    public int getTexture(int var1, int meta) {
        return var1 == 1 ? this.tex - 16 + meta : (var1 == 0 ? this.tex + 16 + meta : this.tex + meta);
    }

    @Override
    public int getMaxColorMeta() {
        return ExBlock.subTypes[this.id];
    }
}
