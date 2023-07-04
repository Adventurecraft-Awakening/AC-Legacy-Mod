package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.block.ExBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class AC_BlockOverlay extends Block implements AC_IBlockColor, AC_ITriggerBlock {

    protected AC_BlockOverlay(int id, int texture) {
        super(id, texture, Material.PLANT);
        this.setBoundingBox(0.0F, 0.0F, 0.0F, 1.0F, 0.1F, 1.0F);
    }

    @Override
    public int getTextureForSide(int var1, int var2) {
        return this.texture + var2;
    }

    @Override
    public AxixAlignedBoundingBox getCollisionShape(World world, int x, int y, int z) {
        this.updateBounds(world, x, y, z);
        return null;
    }

    @Override
    public AxixAlignedBoundingBox getOutlineShape(World world, int x, int y, int z) {
        this.updateBounds(world, x, y, z);
        return super.getOutlineShape(world, x, y, z);
    }

    public void updateBounds(BlockView view, int x, int y, int z) {
        double offset = 1.0 / 64.0;
        double minX = 0.0;
        double minY = 0.0;
        double minZ = 0.0;
        double maxX = 1.0;
        double maxY = 1.0;
        double maxZ = 1.0;
        if (view.method_1783(x, y - 1, z)) {
            maxY = offset;
        } else if (view.method_1783(x, y + 1, z)) {
            minY = 1.0 - offset;
        } else if (view.method_1783(x - 1, y, z)) {
            maxX = offset;
        } else if (view.method_1783(x + 1, y, z)) {
            minX = 1.0 - offset;
        } else if (view.method_1783(x, y, z - 1)) {
            maxZ = offset;
        } else if (view.method_1783(x, y, z + 1)) {
            minZ = 1.0 - offset;
        } else {
            maxY = offset;
        }
        ((ExBlock) this).setBoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public boolean isFullOpaque() {
        return false;
    }

    @Override
    public boolean isCollidable() {
        return AC_DebugMode.active;
    }

    @Override
    public boolean isFullCube() {
        return false;
    }

    @Override
    public int getRenderType() {
        return 37;
    }

    @Override
    public void incrementColor(World world, int x, int y, int z) {
        int meta = world.getBlockMeta(x, y, z);
        world.setBlockMeta(x, y, z, (meta + 1) % ExBlock.subTypes[this.id]);
    }
}
