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
    }

    @Override
    protected BlockEntity createBlockEntity() {
        return new AC_TileEntityTree();
    }

    @Override
    public int getTextureForSide(int var1, int var2) {
        return this.texture + var2;
    }

    @Override
    public AxixAlignedBoundingBox getCollisionShape(World world, int x, int y, int z) {
        return null;
    }

    @Override
    public boolean isCollidable() {
        return AC_DebugMode.active;
    }

    @Override
    public boolean isFullOpaque() {
        return false;
    }

    @Override
    public boolean isFullCube() {
        return false;
    }

    @Override
    public int getRenderType() {
        return 36;
    }

    @Override
    public boolean canUse(World world, int x, int y, int z, PlayerEntity player) {
        if (AC_DebugMode.active) {
            var entity = (AC_TileEntityTree) world.getBlockEntity(x, y, z);
            AC_GuiTree.showUI(world, x, y, z, entity);
        }
        return true;
    }

    @Override
    public void incrementColor(World world, int x, int y, int z) {
        int var5 = world.getBlockMeta(x, y, z);
        world.setBlockMeta(x, y, z, (var5 + 1) % ExBlock.subTypes[this.id]);
    }
}
