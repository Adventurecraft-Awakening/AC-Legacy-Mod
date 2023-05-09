package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.material.Material;
import net.minecraft.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class AC_BlockTimer extends BlockWithEntity implements AC_ITriggerBlock {

    protected AC_BlockTimer(int var1, int var2) {
        super(var1, var2, Material.AIR);
    }

    @Override
    protected BlockEntity createBlockEntity() {
        return new AC_TileEntityTimer();
    }

    @Override
    public boolean isFullOpaque() {
        return false;
    }

    @Override
    public AxixAlignedBoundingBox getCollisionShape(World world, int x, int y, int z) {
        return null;
    }

    @Override
    public boolean shouldRender(BlockView view, int x, int y, int z) {
        return AC_DebugMode.active;
    }

    @Override
    public boolean canBeTriggered() {
        return true;
    }

    public void setTriggerToSelection(World world, int x, int y, int z) {
        var entity = (AC_TileEntityMinMax) world.getBlockEntity(x, y, z);
        if (entity.minX != AC_ItemCursor.minX ||
            entity.minY != AC_ItemCursor.minY ||
            entity.minZ != AC_ItemCursor.minZ ||
            entity.maxX != AC_ItemCursor.maxX ||
            entity.maxY != AC_ItemCursor.maxY ||
            entity.maxZ != AC_ItemCursor.maxZ) {

            entity.minX = AC_ItemCursor.minX;
            entity.minY = AC_ItemCursor.minY;
            entity.minZ = AC_ItemCursor.minZ;
            entity.maxX = AC_ItemCursor.maxX;
            entity.maxY = AC_ItemCursor.maxY;
            entity.maxZ = AC_ItemCursor.maxZ;
        }
    }

    @Override
    public void onTriggerActivated(World world, int x, int y, int z) {
        var entity = (AC_TileEntityTimer) world.getBlockEntity(x, y, z);
        if (entity.canActivate && !entity.active) {
            entity.startActive();
        }
    }

    @Override
    public boolean canUse(World world, int x, int y, int z, PlayerEntity player) {
        if (AC_DebugMode.active) {
            var entity = (AC_TileEntityTimer) world.getBlockEntity(x, y, z);
            AC_GuiTimer.showUI(world, x, y, z, entity);
        }
        return true;
    }

    @Override
    public boolean isCollidable() {
        return AC_DebugMode.active;
    }

    @Override
    public void reset(World world, int x, int y, int z, boolean forDeath) {
        var entity = (AC_TileEntityTimer) world.getBlockEntity(x, y, z);
        entity.active = false;
        entity.canActivate = true;
        entity.ticks = 0;
        ((ExWorld) world).getTriggerManager().removeArea(x, y, z);
    }
}
