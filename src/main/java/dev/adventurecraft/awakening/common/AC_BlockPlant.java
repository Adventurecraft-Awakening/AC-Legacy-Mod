package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.block.ExBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.world.World;

public class AC_BlockPlant extends Block implements AC_IBlockColor {
    protected AC_BlockPlant(int var1, int var2) {
        super(var1, var2, Material.PLANT);
        float var3 = 0.2F;
        this.setBoundingBox(0.5F - var3, 0.0F, 0.5F - var3, 0.5F + var3, var3 * 3.0F, 0.5F + var3);
    }

    public int getTextureForSide(int var1, int var2) {
        return this.texture + var2;
    }

    public AxixAlignedBoundingBox getCollisionShape(World var1, int var2, int var3, int var4) {
        return null;
    }

    public boolean isFullOpaque() {
        return false;
    }

    public boolean isFullCube() {
        return false;
    }

    public int getRenderType() {
        return 1;
    }

    public void incrementColor(World var1, int var2, int var3, int var4) {
        int var5 = var1.getBlockMeta(var2, var3, var4);
        var1.setBlockMeta(var2, var3, var4, (var5 + 1) % ExBlock.subTypes[this.id]);
    }
}
