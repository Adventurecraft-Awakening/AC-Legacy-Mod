package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.block.ExBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;

public class AC_BlockSolid extends Block implements AC_IBlockColor {
    public AC_BlockSolid(int var1, int var2) {
        super(var1, var2, Material.STONE);
    }

    public int getTextureForSide(int var1, int var2) {
        return this.texture + var2;
    }

    public void incrementColor(World var1, int var2, int var3, int var4) {
        int var5 = var1.getBlockMeta(var2, var3, var4);
        var1.setBlockMeta(var2, var3, var4, (var5 + 1) % ExBlock.subTypes[this.id]);
    }
}
