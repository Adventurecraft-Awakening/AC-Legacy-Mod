package dev.adventurecraft.awakening.tile;

import java.util.Random;

import dev.adventurecraft.awakening.common.*;
import dev.adventurecraft.awakening.common.gui.AC_GuiTriggerMemory;
import dev.adventurecraft.awakening.item.AC_ItemCursor;
import dev.adventurecraft.awakening.item.AC_Items;
import dev.adventurecraft.awakening.tile.entity.AC_TileEntityTriggerMemory;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.TileEntityTile;
import net.minecraft.world.level.tile.entity.TileEntity;
import net.minecraft.world.phys.AABB;
import dev.adventurecraft.awakening.extension.world.ExWorld;

public class AC_BlockTriggerMemory extends TileEntityTile implements AC_ITriggerBlock {

    protected AC_BlockTriggerMemory(int id, int texture) {
        super(id, texture, Material.AIR);
    }

    @Override
    protected TileEntity newTileEntity() {
        return new AC_TileEntityTriggerMemory();
    }

    @Override
    public int getResource(int meta, Random rand) {
        return 0;
    }

    @Override
    public int getResourceCount(Random rand) {
        return 0;
    }

    @Override
    public boolean isSolidRender() {
        return false;
    }

    @Override
    public AABB getAABB(Level world, int x, int y, int z) {
        return null;
    }

    @Override
    public boolean shouldRender(LevelSource view, int x, int y, int z) {
        return AC_DebugMode.active;
    }

    @Override
    public boolean mayPick() {
        return AC_DebugMode.active;
    }

    @Override
    public boolean canBeTriggered() {
        return true;
    }

    @Override
    public void onTriggerActivated(Level world, int x, int y, int z) {
        var entity = (AC_TileEntityTriggerMemory) world.getTileEntity(x, y, z);
        if (!entity.isActivated && !entity.activateOnDetrigger) {
            entity.isActivated = true;
            this.triggerActivate(world, x, y, z);
        }
    }

    @Override
    public void onTriggerDeactivated(Level world, int x, int y, int z) {
        var entity = (AC_TileEntityTriggerMemory) world.getTileEntity(x, y, z);
        if (!entity.isActivated && entity.activateOnDetrigger) {
            entity.isActivated = true;
            this.triggerActivate(world, x, y, z);
        }
    }

    public void triggerActivate(Level world, int x, int y, int z) {
        var e = (AC_TileEntityTriggerMemory) world.getTileEntity(x, y, z);
        ((ExWorld) world).getTriggerManager().addArea(
            x, y, z, new AC_TriggerArea(e.minX, e.minY, e.minZ, e.maxX, e.maxY, e.maxZ));
    }

    public void triggerDeactivate(Level world, int x, int y, int z) {
        ((ExWorld) world).getTriggerManager().removeArea(x, y, z);
    }

    @Override
    public void onRemove(Level world, int x, int y, int z) {
        var entity = (AC_TileEntityTriggerMemory) world.getTileEntity(x, y, z);
        if (entity.isSet()) {
            if (world.getData(x, y, z) > 0) {
                this.onTriggerDeactivated(world, x, y, z);
            }
            else {
                this.onTriggerActivated(world, x, y, z);
            }
        }

        super.onRemove(world, x, y, z);
    }

    public void setTriggerToSelection(Level world, int x, int y, int z) {
        var e = (AC_TileEntityTriggerMemory) world.getTileEntity(x, y, z);
        if (e.minX != AC_ItemCursor.minX ||
            e.minY != AC_ItemCursor.minY ||
            e.minZ != AC_ItemCursor.minZ ||
            e.maxX != AC_ItemCursor.maxX ||
            e.maxY != AC_ItemCursor.maxY ||
            e.maxZ != AC_ItemCursor.maxZ) {
            e.set(
                AC_ItemCursor.minX, AC_ItemCursor.minY, AC_ItemCursor.minZ,
                AC_ItemCursor.maxX, AC_ItemCursor.maxY, AC_ItemCursor.maxZ);
        }
    }

    @Override
    public boolean use(Level world, int x, int y, int z, Player player) {
        if (AC_DebugMode.active) {
            ItemInstance item = player.getSelectedItem();
            if (item == null || item.id == AC_Items.cursor.id) {
                var entity = (AC_TileEntityTriggerMemory) world.getTileEntity(x, y, z);
                AC_GuiTriggerMemory.showUI(entity);
                return true;
            }
        }
        return false;
    }

    @Override
    public void tick(Level world, int x, int y, int z, Random rand) {
        var entity = (AC_TileEntityTriggerMemory) world.getTileEntity(x, y, z);
        if (entity.isActivated) {
            this.triggerActivate(world, x, y, z);
        }
    }

    @Override
    public void reset(Level world, int x, int y, int z, boolean forDeath) {
        var entity = (AC_TileEntityTriggerMemory) world.getTileEntity(x, y, z);
        if ((!forDeath || entity.resetOnDeath) && entity.isActivated) {
            entity.isActivated = false;
            this.triggerDeactivate(world, x, y, z);
        }
    }
}
