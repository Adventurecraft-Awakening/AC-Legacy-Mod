package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.block.ExBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;

public class AC_BlockSolid extends Block implements AC_IBlockColor {
    
    public AC_BlockSolid(int var1, int var2) {
        super(var1, var2, Material.STONE);
    }

    @Override
    public int getTextureForSide(int var1, int var2) {
        return this.texture + var2;
    }

    @Override
    public void incrementColor(World world, int x, int y, int z) {
        int var5 = world.getBlockMeta(x, y, z);
        world.setBlockMeta(x, y, z, (var5 + 1) % ExBlock.subTypes[this.id]);
    }
}
