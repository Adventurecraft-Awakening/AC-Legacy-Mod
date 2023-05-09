package dev.adventurecraft.awakening.common;

import java.util.Random;

import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.material.Material;
import net.minecraft.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class AC_BlockTriggerInverter extends BlockWithEntity implements AC_ITriggerBlock {

    protected AC_BlockTriggerInverter(int var1, int var2) {
        super(var1, var2, Material.AIR);
    }

    @Override
    protected BlockEntity createBlockEntity() {
        return new AC_TileEntityTriggerInverter();
    }

    @Override
    public int getDropId(int meta, Random rand) {
        return 0;
    }

    @Override
    public int getDropCount(Random rand) {
        return 0;
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
    public int getTextureForSide(BlockView view, int x, int y, int z, int side) {
        return super.getTextureForSide(view, x, y, z, side);
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
        ((ExWorld) world).getTriggerManager().removeArea(x, y, z);
    }

    @Override
    public void onTriggerDeactivated(World world, int x, int y, int z) {
        var entity = (AC_TileEntityTriggerInverter) world.getBlockEntity(x, y, z);
        ((ExWorld) world).getTriggerManager().addArea(x, y, z, new AC_TriggerArea(entity.minX, entity.minY, entity.minZ, entity.maxX, entity.maxY, entity.maxZ));
    }

    public void setTriggerToSelection(World world, int x, int y, int z) {
        var entity = (AC_TileEntityTriggerInverter) world.getBlockEntity(x, y, z);
        if (entity.minX != AC_ItemCursor.minX ||
            entity.minY != AC_ItemCursor.minY ||
            entity.minZ != AC_ItemCursor.minZ ||
            entity.maxX != AC_ItemCursor.maxX ||
            entity.maxY != AC_ItemCursor.maxY ||
            entity.maxZ != AC_ItemCursor.maxZ) {
            entity.set(AC_ItemCursor.minX, AC_ItemCursor.minY, AC_ItemCursor.minZ, AC_ItemCursor.maxX, AC_ItemCursor.maxY, AC_ItemCursor.maxZ);
        }
    }

    @Override
    public boolean canUse(World world, int x, int y, int z, PlayerEntity player) {
        if (AC_DebugMode.active && player.getHeldItem() != null && player.getHeldItem().itemId == AC_Items.cursor.id) {
            var entity = (AC_TileEntityTriggerInverter) world.getBlockEntity(x, y, z);
            AC_GuiTriggerInverter.showUI(world, x, y, z, entity);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void reset(World world, int x, int y, int z, boolean forDeath) {
        if (!((ExWorld) world).getTriggerManager().isActivated(x, y, z)) {
            this.onTriggerDeactivated(world, x, y, z);
        }
    }
}
