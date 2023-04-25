package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.block.ExBlock;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.material.Material;
import net.minecraft.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.world.World;

public class AC_BlockTree extends BlockWithEntity implements AC_IBlockColor {
    protected AC_BlockTree(int var1, int var2) {
        super(var1, var2, Material.PLANT);
        float var3 = 0.2F;
    }

    protected BlockEntity createBlockEntity() {
        return new AC_TileEntityTree();
    }

    public int getTextureForSide(int var1, int var2) {
        return this.texture + var2;
    }

    public AxixAlignedBoundingBox getCollisionShape(World var1, int var2, int var3, int var4) {
        return null;
    }

    public boolean isCollidable() {
        return AC_DebugMode.active;
    }

    public boolean isFullOpaque() {
        return false;
    }

    public boolean isFullCube() {
        return false;
    }

    public int getRenderType() {
        return 36;
    }

    public boolean canUse(World var1, int var2, int var3, int var4, PlayerEntity var5) {
        if (AC_DebugMode.active) {
            AC_TileEntityTree var6 = (AC_TileEntityTree) var1.getBlockEntity(var2, var3, var4);
            AC_GuiTree.showUI(var1, var2, var3, var4, var6);
        }

        return true;
    }

    public void incrementColor(World var1, int var2, int var3, int var4) {
        int var5 = var1.getBlockMeta(var2, var3, var4);
        var1.setBlockMeta(var2, var3, var4, (var5 + 1) % ExBlock.subTypes[this.id]);
    }
}
