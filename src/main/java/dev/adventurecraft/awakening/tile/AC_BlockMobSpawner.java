package dev.adventurecraft.awakening.tile;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.common.gui.AC_GuiMobSpawner;
import dev.adventurecraft.awakening.item.AC_Items;
import dev.adventurecraft.awakening.tile.entity.AC_TileEntityMobSpawner;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.TileEntityTile;
import net.minecraft.world.level.tile.entity.TileEntity;
import net.minecraft.world.phys.AABB;

import java.util.Random;

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
        if (!AC_DebugMode.active) {
            return false;
        }
        if ((player.getSelectedItem() == null || player.getSelectedItem().id == AC_Items.cursor.id)) {
            if (world.getTileEntity(x, y, z) instanceof AC_TileEntityMobSpawner entityMobSpawner) {
                AC_GuiMobSpawner.showUI(entityMobSpawner);
                return true;
            }
        }
        return false;
    }

    @Override
    public void onTriggerActivated(Level world, int x, int y, int z) {
        if (!(world.getTileEntity(x, y, z) instanceof AC_TileEntityMobSpawner entityMobSpawner)) {
            return;
        }
        if (entityMobSpawner.spawnOnTrigger && !AC_DebugMode.triggerResetActive) {
            entityMobSpawner.spawnMobs();
        }

    }

    @Override
    public void onTriggerDeactivated(Level world, int x, int y, int z) {
        if (!(world.getTileEntity(x, y, z) instanceof AC_TileEntityMobSpawner entityMobSpawner)) {
            return;
        }
        if (entityMobSpawner.spawnOnDetrigger && !AC_DebugMode.triggerResetActive) {
            entityMobSpawner.spawnMobs();
        }

    }

    @Override
    public boolean mayPick() {
        return AC_DebugMode.active;
    }

    @Override
    public void reset(Level world, int x, int y, int z, boolean forDeath) {
        if (!(world.getTileEntity(x, y, z) instanceof AC_TileEntityMobSpawner entityMobSpawner)) {
            return;
        }
        if (!forDeath) {
            entityMobSpawner.hasDroppedItem = false;
        }

        entityMobSpawner.resetMobs();
        entityMobSpawner.delay = 0;
    }
}
