package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.block.ExBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;

public class AC_BlockPillar extends Block implements AC_IBlockColor {

    public AC_BlockPillar(int var1, int var2) {
        super(var1, var2, Material.STONE);
    }

    @Override
    public int getTextureForSide(int var1, int meta) {
        return var1 == 1 ? this.texture - 16 + meta : (var1 == 0 ? this.texture + 16 + meta : this.texture + meta);
    }

    @Override
    public int getMaxColorMeta() {
        return ExBlock.subTypes[this.id];
    }
}
