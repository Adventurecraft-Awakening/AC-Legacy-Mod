package dev.adventurecraft.awakening.tile;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.common.AC_TriggerArea;
import dev.adventurecraft.awakening.common.Coord;
import dev.adventurecraft.awakening.common.gui.AC_GuiTriggerMemory;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.item.AC_ItemCursor;
import dev.adventurecraft.awakening.item.AC_Items;
import dev.adventurecraft.awakening.tile.entity.AC_TileEntityTriggerMemory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.TileEntityTile;
import net.minecraft.world.level.tile.entity.TileEntity;
import net.minecraft.world.phys.AABB;

import java.util.Random;

public class AC_BlockTriggerMemory extends TileEntityTile implements AC_ITriggerDebugBlock {

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
    public boolean mayPick() {
        return AC_DebugMode.active;
    }

    @Override
    public void onTriggerActivated(Level world, int x, int y, int z) {
        if (!(world.getTileEntity(x, y, z) instanceof AC_TileEntityTriggerMemory entityTriggerMemory)) {
            return;
        }
        if (!entityTriggerMemory.isActivated && !entityTriggerMemory.activateOnDetrigger) {
            entityTriggerMemory.isActivated = true;
            this.triggerActivate(world, x, y, z);
        }

    }

    @Override
    public void onTriggerDeactivated(Level world, int x, int y, int z) {
        if (!(world.getTileEntity(x, y, z) instanceof AC_TileEntityTriggerMemory entityTriggerMemory)) {
            return;
        }
        if (!entityTriggerMemory.isActivated && entityTriggerMemory.activateOnDetrigger) {
            entityTriggerMemory.isActivated = true;
            this.triggerActivate(world, x, y, z);
        }
    }

    public void triggerActivate(Level world, int x, int y, int z) {

        if (!(world.getTileEntity(x, y, z) instanceof AC_TileEntityTriggerMemory entityTriggerMemory)) {
            return;
        }
        var area = new AC_TriggerArea(entityTriggerMemory.min(), entityTriggerMemory.max());
        ((ExWorld) world).getTriggerManager().addArea(x, y, z, area);

    }

    public void triggerDeactivate(Level world, int x, int y, int z) {
        ((ExWorld) world).getTriggerManager().removeArea(x, y, z);
    }

    @Override
    public void onRemove(Level world, int x, int y, int z) {
        if (world.getTileEntity(x, y, z) instanceof AC_TileEntityTriggerMemory entityTriggerMemory) {
            if (entityTriggerMemory.isSet()) {
                if (world.getData(x, y, z) > 0) {
                    this.onTriggerDeactivated(world, x, y, z);
                }
                else {
                    this.onTriggerActivated(world, x, y, z);
                }
            }
        }
        super.onRemove(world, x, y, z);
    }

    public void setTriggerToSelection(Level world, int x, int y, int z) {
        if (!(world.getTileEntity(x, y, z) instanceof AC_TileEntityTriggerMemory entityTriggerMemory)) {
            return;
        }
        Coord min = AC_ItemCursor.min();
        Coord max = AC_ItemCursor.max();
        if (!entityTriggerMemory.min().equals(min) || !entityTriggerMemory.max().equals(max)) {
            entityTriggerMemory.set(min, max);
        }
    }

    @Override
    public boolean use(Level world, int x, int y, int z, Player player) {
        if (!AC_DebugMode.active) {
            return false;
        }
        if ((player.getSelectedItem() == null || player.getSelectedItem().id == AC_Items.cursor.id)) {
            if (world.getTileEntity(x, y, z) instanceof AC_TileEntityTriggerMemory entityTriggerMemory) {
                AC_GuiTriggerMemory.showUI(entityTriggerMemory);
                return true;
            }
        }
        return false;
    }

    @Override
    public void tick(Level world, int x, int y, int z, Random rand) {
        if (!(world.getTileEntity(x, y, z) instanceof AC_TileEntityTriggerMemory entityTriggerMemory)) {
            return;
        }
        if (entityTriggerMemory.isActivated) {
            this.triggerActivate(world, x, y, z);
        }
    }

    @Override
    public void reset(Level world, int x, int y, int z, boolean forDeath) {
        if (!(world.getTileEntity(x, y, z) instanceof AC_TileEntityTriggerMemory entityTriggerMemory)) {
            return;
        }
        if ((!forDeath || entityTriggerMemory.resetOnDeath) && entityTriggerMemory.isActivated) {
            entityTriggerMemory.isActivated = false;
            this.triggerDeactivate(world, x, y, z);
        }
    }
}