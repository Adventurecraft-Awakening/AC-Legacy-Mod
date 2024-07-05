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

public class AC_BlockTriggerMemory extends BlockWithEntity implements AC_ITriggerBlock {

    protected AC_BlockTriggerMemory(int id, int texture) {
        super(id, texture, Material.AIR);
    }

    @Override
    protected BlockEntity createBlockEntity() {
        return new AC_TileEntityTriggerMemory();
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
    public boolean isCollidable() {
        return AC_DebugMode.active;
    }

    @Override
    public boolean canBeTriggered() {
        return true;
    }

    @Override
    public void onTriggerActivated(World world, int x, int y, int z) {
        var entity = (AC_TileEntityTriggerMemory) world.getBlockEntity(x, y, z);
        if (!entity.isActivated && !entity.activateOnDetrigger) {
            entity.isActivated = true;
            this.triggerActivate(world, x, y, z);
        }
    }

    @Override
    public void onTriggerDeactivated(World world, int x, int y, int z) {
        var entity = (AC_TileEntityTriggerMemory) world.getBlockEntity(x, y, z);
        if (!entity.isActivated && entity.activateOnDetrigger) {
            entity.isActivated = true;
            this.triggerActivate(world, x, y, z);
        }
    }

    public void triggerActivate(World world, int x, int y, int z) {
        var entity = (AC_TileEntityTriggerMemory) world.getBlockEntity(x, y, z);
        ((ExWorld) world).getTriggerManager().addArea(x, y, z, new AC_TriggerArea(entity.minX, entity.minY, entity.minZ, entity.maxX, entity.maxY, entity.maxZ));
    }

    public void triggerDeactivate(World world, int x, int y, int z) {
        ((ExWorld) world).getTriggerManager().removeArea(x, y, z);
    }

    @Override
    public void onBlockRemoved(World world, int x, int y, int z) {
        var entity = (AC_TileEntityTriggerMemory) world.getBlockEntity(x, y, z);
        if (entity.isSet()) {
            if (world.getBlockMeta(x, y, z) > 0) {
                this.onTriggerDeactivated(world, x, y, z);
            } else {
                this.onTriggerActivated(world, x, y, z);
            }
        }

        super.onBlockRemoved(world, x, y, z);
    }

    public void setTriggerToSelection(World world, int x, int y, int z) {
        var entity = (AC_TileEntityTriggerMemory) world.getBlockEntity(x, y, z);
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
        if (AC_DebugMode.active && (player.getHeldItem() == null || player.getHeldItem().itemId == AC_Items.cursor.id)) {
            var entity = (AC_TileEntityTriggerMemory) world.getBlockEntity(x, y, z);
            AC_GuiTriggerMemory.showUI(world, x, y, z, entity);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onScheduledTick(World world, int x, int y, int z, Random rand) {
        var entity = (AC_TileEntityTriggerMemory) world.getBlockEntity(x, y, z);
        if (entity.isActivated) {
            this.triggerActivate(world, x, y, z);
        }
    }

    @Override
    public void reset(World world, int x, int y, int z, boolean forDeath) {
        var entity = (AC_TileEntityTriggerMemory) world.getBlockEntity(x, y, z);
        if ((!forDeath || entity.resetOnDeath) && entity.isActivated) {
            entity.isActivated = false;
            this.triggerDeactivate(world, x, y, z);
        }
    }
}
