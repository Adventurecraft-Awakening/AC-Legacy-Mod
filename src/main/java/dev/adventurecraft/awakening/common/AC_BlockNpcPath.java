package dev.adventurecraft.awakening.common;

import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.material.Material;
import net.minecraft.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class AC_BlockNpcPath extends BlockWithEntity {

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

    public boolean shouldRender(BlockView var1, int var2, int var3, int var4) {
        return AC_DebugMode.active;
    }

    @Override
    public boolean isCollidable() {
        return AC_DebugMode.active;
    }

    public boolean canBeTriggered() {
        return true;
    }

    public void onTriggerActivated(World var1, int var2, int var3, int var4) {
        AC_TileEntityNpcPath var5 = (AC_TileEntityNpcPath) var1.getBlockEntity(var2, var3, var4);
        if (var5 != null) {
            var5.pathEntity();
        }
    }

    @Override
    public boolean canUse(World var1, int var2, int var3, int var4, PlayerEntity var5) {
        if (AC_DebugMode.active && var5.getHeldItem() != null && var5.getHeldItem().itemId == AC_Items.cursor.id) {
            AC_TileEntityNpcPath var6 = (AC_TileEntityNpcPath) var1.getBlockEntity(var2, var3, var4);
            if (var6 != null) {
                AC_GuiNpcPath.showUI(var6);
            }

            return true;
        } else {
            return false;
        }
    }
}
