package dev.adventurecraft.awakening.common;

import java.util.Random;

import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.material.Material;
import net.minecraft.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class AC_BlockMobSpawner extends BlockWithEntity {

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
    public boolean canUse(World var1, int var2, int var3, int var4, PlayerEntity var5) {
        if (AC_DebugMode.active) {
            AC_TileEntityMobSpawner var6 = (AC_TileEntityMobSpawner) var1.getBlockEntity(var2, var3, var4);
            AC_GuiMobSpawner.showUI(var6);
            return true;
        } else {
            return false;
        }
    }

    public boolean shouldRender(BlockView var1, int var2, int var3, int var4) {
        return AC_DebugMode.active;
    }

    public boolean canBeTriggered() {
        return true;
    }

    public void onTriggerActivated(World var1, int var2, int var3, int var4) {
        AC_TileEntityMobSpawner var5 = (AC_TileEntityMobSpawner) var1.getBlockEntity(var2, var3, var4);
        if (var5.spawnOnTrigger && !AC_DebugMode.triggerResetActive) {
            var5.spawnMobs();
        }
    }

    public void onTriggerDeactivated(World var1, int var2, int var3, int var4) {
        AC_TileEntityMobSpawner var5 = (AC_TileEntityMobSpawner) var1.getBlockEntity(var2, var3, var4);
        if (var5.spawnOnDetrigger && !AC_DebugMode.triggerResetActive) {
            var5.spawnMobs();
        }
    }

    @Override
    public boolean isCollidable() {
        return AC_DebugMode.active;
    }

    public void reset(World var1, int var2, int var3, int var4, boolean var5) {
        AC_TileEntityMobSpawner var6 = (AC_TileEntityMobSpawner) var1.getBlockEntity(var2, var3, var4);
        if (!var5) {
            var6.hasDroppedItem = false;
        }

        var6.resetMobs();
        var6.delay = 0;
    }
}
