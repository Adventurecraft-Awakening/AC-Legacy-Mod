package dev.adventurecraft.awakening.common;

import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.material.Material;
import net.minecraft.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class AC_BlockNpcPath extends BlockWithEntity implements AC_ITriggerBlock {

    public AC_BlockNpcPath(int var1, int var2) {
        super(var1, var2, Material.STONE);
    }

    @Override
    protected BlockEntity createBlockEntity() {
        return new AC_TileEntityNpcPath();
    }

    @Override
    public AxixAlignedBoundingBox getCollisionShape(World var1, int var2, int var3, int var4) {
        return null;
    }

    @Override
    public boolean isFullOpaque() {
        return false;
    }

    @Override
    public boolean shouldRender(BlockView view, int x, int y, int z) {
        return AC_DebugMode.active;
    }

    @Override
    public boolean isCollidable() {
        return AC_DebugMode.active;
    }

    @Override
    public boolean canBeTriggered() {
        return true;
    }

    @Override
    public void onTriggerActivated(World world, int x, int y, int z) {
        var entity = (AC_TileEntityNpcPath) world.getBlockEntity(x, y, z);
        if (entity != null) {
            entity.pathEntity();
        }
    }

    @Override
    public boolean canUse(World world, int x, int y, int z, PlayerEntity player) {
        if (AC_DebugMode.active && player.getHeldItem() != null && player.getHeldItem().itemId == AC_Items.cursor.id) {
            var entity = (AC_TileEntityNpcPath) world.getBlockEntity(x, y, z);
            if (entity != null) {
                AC_GuiNpcPath.showUI(entity);
            }

            return true;
        } else {
            return false;
        }
    }
}
