package dev.adventurecraft.awakening.tile;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.common.AC_TriggerArea;
import dev.adventurecraft.awakening.common.Coord;
import dev.adventurecraft.awakening.common.gui.AC_GuiTrigger;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.item.AC_ItemCursor;
import dev.adventurecraft.awakening.tile.entity.AC_TileEntityMinMax;
import dev.adventurecraft.awakening.tile.entity.AC_TileEntityTrigger;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.TileEntityTile;
import net.minecraft.world.level.tile.entity.TileEntity;
import net.minecraft.world.phys.AABB;

import java.util.Random;

public class AC_BlockTrigger extends TileEntityTile implements AC_ITriggerDebugBlock {

    protected AC_BlockTrigger(int var1, int var2) {
        super(var1, var2, Material.AIR);
    }

    @Override
    protected TileEntity newTileEntity() {
        return new AC_TileEntityTrigger();
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
    public int getTexture(LevelSource view, int x, int y, int z, int side) {
        return super.getTexture(view, x, y, z, side);
    }

    @Override
    public boolean mayPick() {
        return AC_DebugMode.active;
    }

    public @Override boolean canBeTriggered() {
        return false;
    }

    private void setNotVisited(Level world, int x, int y, int z) {
        var entity = ((ExWorld) world).ac$getTileEntity(x, y, z, AC_TileEntityTrigger.class);
        if (!entity.visited) {
            return;
        }
        entity.visited = false;

        for (int bX = x - 1; bX <= x + 1; ++bX) {
            for (int bZ = z - 1; bZ <= z + 1; ++bZ) {
                for (int bY = y - 1; bY <= y + 1; ++bY) {
                    if (world.getTile(bX, bY, bZ) == this.id) {
                        this.setNotVisited(world, bX, bY, bZ);
                    }
                }
            }
        }
    }

    public boolean isAlreadyActivated(Level world, int x, int y, int z) {
        boolean activated = this._isAlreadyActivated(world, x, y, z);
        this.setNotVisited(world, x, y, z);
        return activated;
    }

    private boolean _isAlreadyActivated(Level world, int x, int y, int z) {
        var entity = ((ExWorld) world).ac$getTileEntity(x, y, z, AC_TileEntityTrigger.class);
        if (entity.visited) {
            return false;
        }
        entity.visited = true;
        if (entity.activated > 0) {
            return true;
        }

        for (int bX = x - 1; bX <= x + 1; ++bX) {
            for (int bZ = z - 1; bZ <= z + 1; ++bZ) {
                for (int bY = y - 1; bY <= y + 1; ++bY) {
                    if (world.getTile(bX, bY, bZ) == this.id && this._isAlreadyActivated(world, bX, bY, bZ)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void removeArea(Level world, int x, int y, int z) {
        this._removeArea(world, x, y, z);
        this.setNotVisited(world, x, y, z);
    }

    private void _removeArea(Level world, int x, int y, int z) {
        var entity = ((ExWorld) world).ac$getTileEntity(x, y, z, AC_TileEntityTrigger.class);
        if (entity.visited) {
            return;
        }
        entity.visited = true;
        ((ExWorld) world).getTriggerManager().removeArea(x, y, z);

        for (int bX = x - 1; bX <= x + 1; ++bX) {
            for (int bZ = z - 1; bZ <= z + 1; ++bZ) {
                for (int bY = y - 1; bY <= y + 1; ++bY) {
                    if (world.getTile(bX, bY, bZ) == this.id) {
                        this._removeArea(world, bX, bY, bZ);
                    }
                }
            }
        }
    }

    @Override
    public void entityInside(Level world, int x, int y, int z, Entity entity) {
        if (AC_DebugMode.active) {
            return;
        }
        if (!(entity instanceof Player)) {
            return;
        }

        var e = ((ExWorld) world).ac$getTileEntity(x, y, z, AC_TileEntityTrigger.class);

        // Treat trigger blocks with no set as if they are not trigger blocks
        if (!e.isSet()) {
            return;
        }

        if (!this.isAlreadyActivated(world, x, y, z)) {
            if (!e.resetOnTrigger) {
                var area = new AC_TriggerArea(e.min(), e.max());
                ((ExWorld) world).getTriggerManager().addArea(x, y, z, area);
            }
            else {
                ExBlock.resetArea(world, e.min(), e.max());
            }
        }

        // If player is dead, set activated to 1 so that the triggerArea can be removed in AC_TileEntityTrigger!
        if (((Player) entity).health <= 0) {
            e.activated = 1;
        }
        else {
            e.activated = 2;
        }
    }

    public void deactivateTrigger(Level world, int x, int y, int z) {
        if (this.isAlreadyActivated(world, x, y, z)) {
            return;
        }

        var entity = ((ExWorld) world).ac$getTileEntity(x, y, z, AC_TileEntityTrigger.class);
        if (!entity.resetOnTrigger) {
            this.removeArea(world, x, y, z);
        }
    }

    public void setTriggerToSelection(Level world, int x, int y, int z) {
        var e = ((ExWorld) world).ac$getTileEntity(x, y, z, AC_TileEntityMinMax.class);
        Coord min = AC_ItemCursor.min();
        Coord max = AC_ItemCursor.max();
        if (e.min().equals(min) && e.max().equals(max)) {
            return;
        }
        e.setMin(min);
        e.setMax(max);

        for (int bX = x - 1; bX <= x + 1; ++bX) {
            for (int bZ = z - 1; bZ <= z + 1; ++bZ) {
                for (int bY = y - 1; bY <= y + 1; ++bY) {
                    if (world.getTile(bX, bY, bZ) == this.id) {
                        this.setTriggerToSelection(world, bX, bY, bZ);
                    }
                }
            }
        }
    }

    public void setTriggerReset(Level world, int x, int y, int z, boolean reset) {
        var entity = ((ExWorld) world).ac$getTileEntity(x, y, z, AC_TileEntityTrigger.class);
        if (entity.resetOnTrigger == reset) {
            return;
        }
        entity.resetOnTrigger = reset;

        for (int bX = x - 1; bX <= x + 1; ++bX) {
            for (int bZ = z - 1; bZ <= z + 1; ++bZ) {
                for (int bY = y - 1; bY <= y + 1; ++bY) {
                    if (world.getTile(bX, bY, bZ) == this.id) {
                        this.setTriggerReset(world, bX, bY, bZ, reset);
                    }
                }
            }
        }
    }

    @Override
    public boolean use(Level world, int x, int y, int z, Player player) {
        if (!AC_DebugMode.showDebugGuiOnUse(player)) {
            return false;
        }
        var entity = ((ExWorld) world).ac$getTileEntity(x, y, z, AC_TileEntityTrigger.class);
        AC_GuiTrigger.showUI(entity);
        return true;
    }

    @Override
    public void reset(Level world, int x, int y, int z, boolean forDeath) {
        var entity = ((ExWorld) world).ac$getTileEntity(x, y, z, AC_TileEntityTrigger.class);
        entity.activated = 0;
    }
}
