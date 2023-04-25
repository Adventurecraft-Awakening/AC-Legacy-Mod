package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.material.Material;
import net.minecraft.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class AC_BlockRedstoneTrigger extends BlockWithEntity {
    protected AC_BlockRedstoneTrigger(int var1, int var2) {
        super(var1, var2, Material.STONE);
    }

    protected BlockEntity createBlockEntity() {
        return new AC_TileEntityRedstoneTrigger();
    }

    public void onAdjacentBlockUpdate(World var1, int var2, int var3, int var4, int var5) {
        this.updateBlock(var1, var2, var3, var4, var5);
    }

    public int getTextureForSide(BlockView var1, int var2, int var3, int var4, int var5) {
        AC_TileEntityRedstoneTrigger var6 = (AC_TileEntityRedstoneTrigger) var1.getBlockEntity(var2, var3, var4);
        return var6.isActivated ? this.texture : this.texture + 1;
    }

    private void updateBlock(World var1, int var2, int var3, int var4, int var5) {
        boolean var6 = var1.hasRedstonePower(var2, var3, var4);
        AC_TileEntityRedstoneTrigger var7 = (AC_TileEntityRedstoneTrigger) var1.getBlockEntity(var2, var3, var4);
        if (var7 != null && var7.isActivated != var6) {
            var7.isActivated = var6;
            var1.notifyListeners(var2, var3, var4);
            if (var6) {
                if (!var7.resetOnTrigger) {
                    ((ExWorld) var1).getTriggerManager().addArea(var2, var3, var4, new AC_TriggerArea(var7.minX, var7.minY, var7.minZ, var7.maxX, var7.maxY, var7.maxZ));
                } else {
                    ExBlock.resetArea(var1, var7.minX, var7.minY, var7.minZ, var7.maxX, var7.maxY, var7.maxZ);
                }
            } else {
                ((ExWorld) var1).getTriggerManager().removeArea(var2, var3, var4);
            }
        }
    }

    public boolean canUse(World var1, int var2, int var3, int var4, PlayerEntity var5) {
        if (AC_DebugMode.active && var5.getHeldItem() != null && var5.getHeldItem().itemId == AC_Items.cursor.id) {
            AC_TileEntityRedstoneTrigger var6 = (AC_TileEntityRedstoneTrigger) var1.getBlockEntity(var2, var3, var4);
            AC_GuiRedstoneTrigger.showUI(var1, var2, var3, var4, var6);
        }

        return true;
    }

    public void setTriggerToSelection(World var1, int var2, int var3, int var4) {
        AC_TileEntityRedstoneTrigger var5 = (AC_TileEntityRedstoneTrigger) var1.getBlockEntity(var2, var3, var4);
        if (var5.minX != AC_ItemCursor.minX || var5.minY != AC_ItemCursor.minY || var5.minZ != AC_ItemCursor.minZ || var5.maxX != AC_ItemCursor.maxX || var5.maxY != AC_ItemCursor.maxY || var5.maxZ != AC_ItemCursor.maxZ) {
            var5.minX = AC_ItemCursor.minX;
            var5.minY = AC_ItemCursor.minY;
            var5.minZ = AC_ItemCursor.minZ;
            var5.maxX = AC_ItemCursor.maxX;
            var5.maxY = AC_ItemCursor.maxY;
            var5.maxZ = AC_ItemCursor.maxZ;
        }
    }
}
