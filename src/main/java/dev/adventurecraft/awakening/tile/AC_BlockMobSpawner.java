package dev.adventurecraft.awakening.tile;

import java.util.Random;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.tile.entity.AC_TileEntityMobSpawner;
import dev.adventurecraft.awakening.common.gui.AC_GuiMobSpawner;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.TileEntityTile;
import net.minecraft.world.level.tile.entity.TileEntity;
import net.minecraft.world.phys.AABB;

public class AC_BlockMobSpawner extends TileEntityTile implements AC_ITriggerDebugBlock {

    protected AC_BlockMobSpawner(int var1, int var2) {
        super(var1, var2, Material.AIR);
    }

    @Override
    protected TileEntity newTileEntity() {
        return new AC_TileEntityMobSpawner();
    }

    @Override
    public int getResource(int var1, Random var2) {
        return 0;
    }

    @Override
    public int getResourceCount(Random var1) {
        return 0;
    }

    @Override
    public boolean isSolidRender() {
        return false;
    }

    @Override
    public AABB getAABB(Level var1, int var2, int var3, int var4) {
        return null;
    }

    @Override
    public boolean use(Level world, int x, int y, int z, Player player) {
        if (AC_DebugMode.active) {
            var entity = (AC_TileEntityMobSpawner) world.getTileEntity(x, y, z);
            AC_GuiMobSpawner.showUI(entity);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onTriggerActivated(Level world, int x, int y, int z) {
        var entity = (AC_TileEntityMobSpawner) world.getTileEntity(x, y, z);
        if (entity.spawnOnTrigger && !AC_DebugMode.triggerResetActive) {
            entity.spawnMobs();
        }
    }

    @Override
    public void onTriggerDeactivated(Level world, int x, int y, int z) {
        var entity = (AC_TileEntityMobSpawner) world.getTileEntity(x, y, z);
        if (entity.spawnOnDetrigger && !AC_DebugMode.triggerResetActive) {
            entity.spawnMobs();
        }
    }

    @Override
    public boolean mayPick() {
        return AC_DebugMode.active;
    }

    @Override
    public void reset(Level world, int x, int y, int z, boolean forDeath) {
        var entity = (AC_TileEntityMobSpawner) world.getTileEntity(x, y, z);
        if (!forDeath) {
            entity.hasDroppedItem = false;
        }

        entity.resetMobs();
        entity.delay = 0;
    }
}
