package dev.adventurecraft.awakening.common;

import java.util.Random;

import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.material.Material;
import net.minecraft.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class AC_BlockTrigger extends BlockWithEntity implements AC_ITriggerBlock {

    protected AC_BlockTrigger(int var1, int var2) {
        super(var1, var2, Material.AIR);
    }

    @Override
    protected BlockEntity createBlockEntity() {
        return new AC_TileEntityTrigger();
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

    private void setNotVisited(World world, int x, int y, int z) {
        var entity = (AC_TileEntityTrigger) world.getBlockEntity(x, y, z);
        if (entity == null || !entity.visited) {
            return;
        }
        entity.visited = false;

        for (int bX = x - 1; bX <= x + 1; ++bX) {
            for (int bZ = z - 1; bZ <= z + 1; ++bZ) {
                for (int bY = y - 1; bY <= y + 1; ++bY) {
                    if (world.getBlockId(bX, bY, bZ) == this.id) {
                        this.setNotVisited(world, bX, bY, bZ);
                    }
                }
            }
        }
    }

    public boolean isAlreadyActivated(World world, int x, int y, int z) {
        boolean activated = this._isAlreadyActivated(world, x, y, z);
        this.setNotVisited(world, x, y, z);
        return activated;
    }

    private boolean _isAlreadyActivated(World world, int x, int y, int z) {
        var entity = (AC_TileEntityTrigger) world.getBlockEntity(x, y, z);
        if (entity == null || entity.visited) {
            return false;
        }
        entity.visited = true;

        if (entity.activated > 0) {
            return true;
        }

        for (int bX = x - 1; bX <= x + 1; ++bX) {
            for (int bZ = z - 1; bZ <= z + 1; ++bZ) {
                for (int bY = y - 1; bY <= y + 1; ++bY) {
                    if (world.getBlockId(bX, bY, bZ) == this.id && this._isAlreadyActivated(world, bX, bY, bZ)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public void removeArea(World world, int x, int y, int z) {
        this._removeArea(world, x, y, z);
        this.setNotVisited(world, x, y, z);
    }

    private void _removeArea(World world, int x, int y, int z) {
        var entity = (AC_TileEntityTrigger) world.getBlockEntity(x, y, z);
        if (entity.visited) {
            return;
        }
        entity.visited = true;
        ((ExWorld) world).getTriggerManager().removeArea(x, y, z);

        for (int bX = x - 1; bX <= x + 1; ++bX) {
            for (int bZ = z - 1; bZ <= z + 1; ++bZ) {
                for (int bY = y - 1; bY <= y + 1; ++bY) {
                    if (world.getBlockId(bX, bY, bZ) == this.id) {
                        this._removeArea(world, bX, bY, bZ);
                    }
                }
            }
        }
    }

    @Override
    public void onEntityCollision(World world, int x, int y, int z, Entity entity) {
        if (AC_DebugMode.active) {
            return;
        }
        if (!(entity instanceof PlayerEntity)) {
            return;
        }

        var tileEntity = (AC_TileEntityTrigger) world.getBlockEntity(x, y, z);
        if (!this.isAlreadyActivated(world, x, y, z)) {
            if (!tileEntity.resetOnTrigger) {
                ((ExWorld) world).getTriggerManager().addArea(x, y, z, new AC_TriggerArea(tileEntity.minX, tileEntity.minY, tileEntity.minZ, tileEntity.maxX, tileEntity.maxY, tileEntity.maxZ));
            } else {
                ExBlock.resetArea(world, tileEntity.minX, tileEntity.minY, tileEntity.minZ, tileEntity.maxX, tileEntity.maxY, tileEntity.maxZ);
            }
        }
        // If player is dead, set activated to 1 so that the triggerArea can be removed in AC_TileEntityTrigger!
        if (((PlayerEntity) entity).health <= 0){
            tileEntity.activated = 1;
        }
        else {
            tileEntity.activated = 2;
        }
    }

    public void deactivateTrigger(World world, int x, int y, int z) {
        if (this.isAlreadyActivated(world, x, y, z)) {
            return;
        }

        AC_TileEntityTrigger tileEntity = (AC_TileEntityTrigger) world.getBlockEntity(x, y, z);
        if (!tileEntity.resetOnTrigger) {
            this.removeArea(world, x, y, z);
        }
    }

    public void setTriggerToSelection(World world, int x, int y, int z) {
        AC_TileEntityMinMax tileEntity = (AC_TileEntityMinMax) world.getBlockEntity(x, y, z);
        if (tileEntity.minX == AC_ItemCursor.minX &&
            tileEntity.minY == AC_ItemCursor.minY &&
            tileEntity.minZ == AC_ItemCursor.minZ &&
            tileEntity.maxX == AC_ItemCursor.maxX &&
            tileEntity.maxY == AC_ItemCursor.maxY &&
            tileEntity.maxZ == AC_ItemCursor.maxZ) {
            return;
        }

        tileEntity.minX = AC_ItemCursor.minX;
        tileEntity.minY = AC_ItemCursor.minY;
        tileEntity.minZ = AC_ItemCursor.minZ;
        tileEntity.maxX = AC_ItemCursor.maxX;
        tileEntity.maxY = AC_ItemCursor.maxY;
        tileEntity.maxZ = AC_ItemCursor.maxZ;

        for (int bX = x - 1; bX <= x + 1; ++bX) {
            for (int bZ = z - 1; bZ <= z + 1; ++bZ) {
                for (int bY = y - 1; bY <= y + 1; ++bY) {
                    if (world.getBlockId(bX, bY, bZ) == this.id) {
                        this.setTriggerToSelection(world, bX, bY, bZ);
                    }
                }
            }
        }
    }

    public void setTriggerReset(World world, int x, int y, int z, boolean reset) {
        var entity = (AC_TileEntityTrigger) world.getBlockEntity(x, y, z);
        if (entity.resetOnTrigger == reset) {
            return;
        }
        entity.resetOnTrigger = reset;

        for (int bX = x - 1; bX <= x + 1; ++bX) {
            for (int bZ = z - 1; bZ <= z + 1; ++bZ) {
                for (int bY = y - 1; bY <= y + 1; ++bY) {
                    if (world.getBlockId(bX, bY, bZ) == this.id) {
                        this.setTriggerReset(world, bX, bY, bZ, reset);
                    }
                }
            }
        }
    }

    @Override
    public boolean canUse(World world, int x, int y, int z, PlayerEntity player) {
        // #79 place trigger blocks on trigger blocks + #5 open trigger gui with hand or cursor, everything else (see else) places the block
        if (AC_DebugMode.active  && (player.getHeldItem() == null || player.getHeldItem().itemId == AC_Items.cursor.id)) {
            var entity = (AC_TileEntityTrigger) world.getBlockEntity(x, y, z);
            AC_GuiTrigger.showUI(world, x, y, z, entity);
        }
        else {
            return false;
        }
        return true;
    }

    @Override
    public void reset(World world, int x, int y, int z, boolean forDeath) {
        AC_TileEntityTrigger tileEntity = (AC_TileEntityTrigger) world.getBlockEntity(x, y, z);
        tileEntity.activated = 0;
    }
}
