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

public class AC_BlockTrigger extends BlockWithEntity {
    protected AC_BlockTrigger(int var1, int var2) {
        super(var1, var2, Material.AIR);
    }

    protected BlockEntity createBlockEntity() {
        return new AC_TileEntityTrigger();
    }

    public int getDropId(int var1, Random var2) {
        return 0;
    }

    public int getDropCount(Random var1) {
        return 0;
    }

    public boolean isFullOpaque() {
        return false;
    }

    public AxixAlignedBoundingBox getCollisionShape(World var1, int var2, int var3, int var4) {
        return null;
    }

    public boolean shouldRender(BlockView var1, int var2, int var3, int var4) {
        return AC_DebugMode.active;
    }

    public int getTextureForSide(BlockView var1, int var2, int var3, int var4, int var5) {
        return super.getTextureForSide(var1, var2, var3, var4, var5);
    }

    public boolean isCollidable() {
        return AC_DebugMode.active;
    }

    private void setNotVisited(World world, int x, int y, int z) {
        AC_TileEntityTrigger tileEntity = (AC_TileEntityTrigger) world.getBlockEntity(x, y, z);
        if (tileEntity == null || !tileEntity.visited) {
            return;
        }
        tileEntity.visited = false;

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
        AC_TileEntityTrigger tileEntity = (AC_TileEntityTrigger) world.getBlockEntity(x, y, z);
        if (tileEntity == null || tileEntity.visited) {
            return false;
        }
        tileEntity.visited = true;

        if (tileEntity.activated > 0) {
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
        AC_TileEntityTrigger tileEntity = (AC_TileEntityTrigger) world.getBlockEntity(x, y, z);
        if (tileEntity.visited) {
            return;
        }
        tileEntity.visited = true;
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

    public void onEntityCollision(World world, int x, int y, int z, Entity entity) {
        if (AC_DebugMode.active) {
            return;
        }
        if (!(entity instanceof PlayerEntity)) {
            return;
        }

        AC_TileEntityTrigger tileEntity = (AC_TileEntityTrigger) world.getBlockEntity(x, y, z);
        if (!this.isAlreadyActivated(world, x, y, z)) {
            if (!tileEntity.resetOnTrigger) {
                ((ExWorld) world).getTriggerManager().addArea(x, y, z, new AC_TriggerArea(tileEntity.minX, tileEntity.minY, tileEntity.minZ, tileEntity.maxX, tileEntity.maxY, tileEntity.maxZ));
            } else {
                ExBlock.resetArea(world, tileEntity.minX, tileEntity.minY, tileEntity.minZ, tileEntity.maxX, tileEntity.maxY, tileEntity.maxZ);
            }
        }
        tileEntity.activated = 2;
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
        AC_TileEntityTrigger tileEntity = (AC_TileEntityTrigger) world.getBlockEntity(x, y, z);
        if (tileEntity.resetOnTrigger == reset) {
            return;
        }
        tileEntity.resetOnTrigger = reset;

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

    public boolean canUse(World world, int x, int y, int z, PlayerEntity player) {
        if (AC_DebugMode.active && player.getHeldItem() != null && player.getHeldItem().itemId == AC_Items.cursor.id) {
            AC_TileEntityTrigger tileEntity = (AC_TileEntityTrigger) world.getBlockEntity(x, y, z);
            AC_GuiTrigger.showUI(world, x, y, z, tileEntity);
        }
        return true;
    }

    public void reset(World world, int x, int y, int z, boolean var5) {
        AC_TileEntityTrigger tileEntity = (AC_TileEntityTrigger) world.getBlockEntity(x, y, z);
        tileEntity.activated = 0;
    }
}
