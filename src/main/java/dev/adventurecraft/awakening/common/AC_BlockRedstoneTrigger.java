package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.material.Material;
import net.minecraft.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class AC_BlockRedstoneTrigger extends BlockWithEntity implements AC_ITriggerBlock {

    protected AC_BlockRedstoneTrigger(int var1, int var2) {
        super(var1, var2, Material.STONE);
    }

    @Override
    protected BlockEntity createBlockEntity() {
        return new AC_TileEntityRedstoneTrigger();
    }

    @Override
    public void onAdjacentBlockUpdate(World world, int x, int y, int z, int id) {
        this.updateBlock(world, x, y, z, id);
    }

    @Override
    public int getTextureForSide(BlockView view, int x, int y, int z, int side) {
        var entity = (AC_TileEntityRedstoneTrigger) view.getBlockEntity(x, y, z);
        return entity.isActivated ? this.texture : this.texture + 1;
    }

    private void updateBlock(World world, int x, int y, int z, int side) {
        boolean isActivated = world.hasRedstonePower(x, y, z);
        var entity = (AC_TileEntityRedstoneTrigger) world.getBlockEntity(x, y, z);
        if (entity != null && entity.isActivated != isActivated) {
            entity.isActivated = isActivated;
            world.notifyListeners(x, y, z);
            if (isActivated) {
                if (!entity.resetOnTrigger) {
                    ((ExWorld) world).getTriggerManager().addArea(x, y, z, new AC_TriggerArea(entity.minX, entity.minY, entity.minZ, entity.maxX, entity.maxY, entity.maxZ));
                } else {
                    ExBlock.resetArea(world, entity.minX, entity.minY, entity.minZ, entity.maxX, entity.maxY, entity.maxZ);
                }
            } else {
                ((ExWorld) world).getTriggerManager().removeArea(x, y, z);
            }
        }
    }

    @Override
    public boolean canUse(World world, int x, int y, int z, PlayerEntity player) {
        if (AC_DebugMode.active && (player.getHeldItem() == null || player.getHeldItem().itemId == AC_Items.cursor.id)) {
            var entity = (AC_TileEntityRedstoneTrigger) world.getBlockEntity(x, y, z);
            AC_GuiRedstoneTrigger.showUI(world, x, y, z, entity);
            return true;
        } else {
            return false;
        }
    }

    public void setTriggerToSelection(World world, int x, int y, int z) {
        var entity = (AC_TileEntityRedstoneTrigger) world.getBlockEntity(x, y, z);
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
}
