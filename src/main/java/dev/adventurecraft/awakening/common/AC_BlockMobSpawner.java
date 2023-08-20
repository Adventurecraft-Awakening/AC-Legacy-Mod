package dev.adventurecraft.awakening.common;

import java.util.Random;

import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.material.Material;
import net.minecraft.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class AC_BlockMobSpawner extends BlockWithEntity implements AC_ITriggerBlock {

    protected AC_BlockMobSpawner(int var1, int var2) {
        super(var1, var2, Material.AIR);
    }

    @Override
    protected BlockEntity createBlockEntity() {
        return new AC_TileEntityMobSpawner();
    }

    @Override
    public int getDropId(int var1, Random var2) {
        return 0;
    }

    @Override
    public int getDropCount(Random var1) {
        return 0;
    }

    @Override
    public boolean isFullOpaque() {
        return false;
    }

    @Override
    public AxixAlignedBoundingBox getCollisionShape(World var1, int var2, int var3, int var4) {
        return null;
    }

    @Override
    public boolean canUse(World world, int x, int y, int z, PlayerEntity player) {
        if (AC_DebugMode.active) {
            var entity = (AC_TileEntityMobSpawner) world.getBlockEntity(x, y, z);
            AC_GuiMobSpawner.showUI(entity);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean shouldRender(BlockView view, int x, int y, int z) {
        return AC_DebugMode.active;
    }

    @Override
    public boolean canBeTriggered() {
        return true;
    }

    @Override
    public void onTriggerActivated(World world, int x, int y, int z) {
        var entity = (AC_TileEntityMobSpawner) world.getBlockEntity(x, y, z);
        if (entity.spawnOnTrigger && !AC_DebugMode.triggerResetActive) {
            entity.spawnMobs();
        }
    }

    @Override
    public void onTriggerDeactivated(World world, int x, int y, int z) {
        var entity = (AC_TileEntityMobSpawner) world.getBlockEntity(x, y, z);
        if (entity.spawnOnDetrigger && !AC_DebugMode.triggerResetActive) {
            entity.spawnMobs();
        }
    }

    @Override
    public boolean isCollidable() {
        return AC_DebugMode.active;
    }

    @Override
    public void reset(World world, int x, int y, int z, boolean forDeath) {
        var entity = (AC_TileEntityMobSpawner) world.getBlockEntity(x, y, z);
        if (!forDeath) {
            entity.hasDroppedItem = false;
        }

        entity.resetMobs();
        entity.delay = 0;
    }
}
