package dev.adventurecraft.awakening.common;

import java.util.Random;

import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.block.Block;
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

    private void setNotVisited(World var1, int var2, int var3, int var4) {
        AC_TileEntityTrigger var5 = (AC_TileEntityTrigger) var1.getBlockEntity(var2, var3, var4);
        if (var5 != null && var5.visited) {
            var5.visited = false;

            for (int var6 = var2 - 1; var6 <= var2 + 1; ++var6) {
                for (int var7 = var3 - 1; var7 <= var3 + 1; ++var7) {
                    for (int var8 = var4 - 1; var8 <= var4 + 1; ++var8) {
                        if (var1.getBlockId(var6, var7, var8) == this.id) {
                            this.setNotVisited(var1, var6, var7, var8);
                        }
                    }
                }
            }
        }

    }

    public boolean isAlreadyActivated(World var1, int var2, int var3, int var4) {
        boolean var5 = this._isAlreadyActivated(var1, var2, var3, var4);
        this.setNotVisited(var1, var2, var3, var4);
        return var5;
    }

    private boolean _isAlreadyActivated(World var1, int var2, int var3, int var4) {
        boolean var5 = false;
        AC_TileEntityTrigger var6 = (AC_TileEntityTrigger) var1.getBlockEntity(var2, var3, var4);
        if (var6 != null && !var6.visited) {
            var6.visited = true;
            if (var6.activated > 0) {
                return true;
            }

            for (int var7 = var2 - 1; var7 <= var2 + 1; ++var7) {
                for (int var8 = var3 - 1; var8 <= var3 + 1; ++var8) {
                    for (int var9 = var4 - 1; var9 <= var4 + 1; ++var9) {
                        if (var1.getBlockId(var7, var8, var9) == this.id && this._isAlreadyActivated(var1, var7, var8, var9)) {
                            var5 = true;
                            break;
                        }
                    }

                    if (var5) {
                        break;
                    }
                }

                if (var5) {
                    break;
                }
            }
        }

        return var5;
    }

    public void removeArea(World var1, int var2, int var3, int var4) {
        this._removeArea(var1, var2, var3, var4);
        this.setNotVisited(var1, var2, var3, var4);
    }

    private void _removeArea(World var1, int var2, int var3, int var4) {
        AC_TileEntityTrigger var5 = (AC_TileEntityTrigger) var1.getBlockEntity(var2, var3, var4);
        if (!var5.visited) {
            var5.visited = true;
            ((ExWorld) var1).getTriggerManager().removeArea(var2, var3, var4);

            for (int var6 = var2 - 1; var6 <= var2 + 1; ++var6) {
                for (int var7 = var3 - 1; var7 <= var3 + 1; ++var7) {
                    for (int var8 = var4 - 1; var8 <= var4 + 1; ++var8) {
                        if (var1.getBlockId(var6, var7, var8) == this.id) {
                            this._removeArea(var1, var6, var7, var8);
                        }
                    }
                }
            }
        }

    }

    public void onEntityCollision(World var1, int var2, int var3, int var4, Entity var5) {
        if (!AC_DebugMode.active) {
            AC_TileEntityTrigger var6 = (AC_TileEntityTrigger) var1.getBlockEntity(var2, var3, var4);
            if (var5 instanceof PlayerEntity) {
                if (!this.isAlreadyActivated(var1, var2, var3, var4)) {
                    if (!var6.resetOnTrigger) {
                        ((ExWorld) var1).getTriggerManager().addArea(var2, var3, var4, new AC_TriggerArea(var6.minX, var6.minY, var6.minZ, var6.maxX, var6.maxY, var6.maxZ));
                    } else {
                        ExBlock.resetArea(var1, var6.minX, var6.minY, var6.minZ, var6.maxX, var6.maxY, var6.maxZ);
                    }
                }

                var6.activated = 2;
            }

        }
    }

    public void deactivateTrigger(World var1, int var2, int var3, int var4) {
        AC_TileEntityTrigger var5 = (AC_TileEntityTrigger) var1.getBlockEntity(var2, var3, var4);
        if (!this.isAlreadyActivated(var1, var2, var3, var4) && !var5.resetOnTrigger) {
            this.removeArea(var1, var2, var3, var4);
        }

    }

    public void setTriggerToSelection(World var1, int var2, int var3, int var4) {
        AC_TileEntityMinMax var5 = (AC_TileEntityMinMax) var1.getBlockEntity(var2, var3, var4);
        if (var5.minX != AC_ItemCursor.minX || var5.minY != AC_ItemCursor.minY || var5.minZ != AC_ItemCursor.minZ || var5.maxX != AC_ItemCursor.maxX || var5.maxY != AC_ItemCursor.maxY || var5.maxZ != AC_ItemCursor.maxZ) {
            var5.minX = AC_ItemCursor.minX;
            var5.minY = AC_ItemCursor.minY;
            var5.minZ = AC_ItemCursor.minZ;
            var5.maxX = AC_ItemCursor.maxX;
            var5.maxY = AC_ItemCursor.maxY;
            var5.maxZ = AC_ItemCursor.maxZ;

            for (int var6 = var2 - 1; var6 <= var2 + 1; ++var6) {
                for (int var7 = var3 - 1; var7 <= var3 + 1; ++var7) {
                    for (int var8 = var4 - 1; var8 <= var4 + 1; ++var8) {
                        if (var1.getBlockId(var6, var7, var8) == this.id) {
                            this.setTriggerToSelection(var1, var6, var7, var8);
                        }
                    }
                }
            }

        }
    }

    public void setTriggerReset(World var1, int var2, int var3, int var4, boolean var5) {
        AC_TileEntityTrigger var6 = (AC_TileEntityTrigger) var1.getBlockEntity(var2, var3, var4);
        if (var6.resetOnTrigger != var5) {
            var6.resetOnTrigger = var5;

            for (int var7 = var2 - 1; var7 <= var2 + 1; ++var7) {
                for (int var8 = var3 - 1; var8 <= var3 + 1; ++var8) {
                    for (int var9 = var4 - 1; var9 <= var4 + 1; ++var9) {
                        if (var1.getBlockId(var7, var8, var9) == this.id) {
                            this.setTriggerReset(var1, var7, var8, var9, var5);
                        }
                    }
                }
            }

        }
    }

    public boolean canUse(World var1, int var2, int var3, int var4, PlayerEntity var5) {
        if (AC_DebugMode.active && var5.getHeldItem() != null && var5.getHeldItem().itemId == AC_Items.cursor.id) {
            AC_TileEntityTrigger var6 = (AC_TileEntityTrigger) var1.getBlockEntity(var2, var3, var4);
            AC_GuiTrigger.showUI(var1, var2, var3, var4, var6);
        }

        return true;
    }

    public void reset(World var1, int var2, int var3, int var4, boolean var5) {
        AC_TileEntityTrigger var6 = (AC_TileEntityTrigger) var1.getBlockEntity(var2, var3, var4);
        var6.activated = 0;
    }
}
