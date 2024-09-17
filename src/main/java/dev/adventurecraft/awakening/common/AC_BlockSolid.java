package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.block.ExBlock;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.Tile;

public class AC_BlockSolid extends Tile implements AC_IBlockColor {
    
    public AC_BlockSolid(int var1, int var2) {
        super(var1, var2, Material.STONE);
    }

    @Override
    public int getTexture(int var1, int var2) {
        return this.tex + var2;
    }

    @Override
    public int getMaxColorMeta() {
        return ExBlock.subTypes[this.id];
    }
}
