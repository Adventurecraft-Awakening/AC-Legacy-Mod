package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.material.Material;
import net.minecraft.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class AC_BlockTriggerPushable extends BlockWithEntity implements AC_IBlockColor {

    protected AC_BlockTriggerPushable(int var1, int var2) {
        super(var1, var2, Material.STONE);
    }

    @Override
    protected BlockEntity createBlockEntity() {
        return new AC_TileEntityTriggerPushable();
    }

    private boolean checkBlock(World var1, int var2, int var3, int var4, int var5) {
        return var1.getBlockId(var2, var3, var4) == AC_Blocks.pushableBlock.id && var1.getBlockMeta(var2, var3, var4) == var5;
    }

    @Override
    public void onAdjacentBlockUpdate(World var1, int var2, int var3, int var4, int var5) {
        AC_TileEntityTriggerPushable var6 = (AC_TileEntityTriggerPushable) var1.getBlockEntity(var2, var3, var4);
        int var7 = var1.getBlockMeta(var2, var3, var4);
        boolean var8 = this.checkBlock(var1, var2 + 1, var3, var4, var7);
        var8 |= this.checkBlock(var1, var2 - 1, var3, var4, var7);
        var8 |= this.checkBlock(var1, var2, var3 + 1, var4, var7);
        var8 |= this.checkBlock(var1, var2, var3 - 1, var4, var7);
        var8 |= this.checkBlock(var1, var2, var3, var4 + 1, var7);
        var8 |= this.checkBlock(var1, var2, var3, var4 - 1, var7);
        if (var6.activated) {
            if (!var8) {
                var6.activated = false;
                ((ExWorld) var1).getTriggerManager().removeArea(var2, var3, var4);
            }
        } else if (var8) {
            var6.activated = true;
            if (!var6.resetOnTrigger) {
                ((ExWorld) var1).getTriggerManager().addArea(var2, var3, var4, new AC_TriggerArea(var6.minX, var6.minY, var6.minZ, var6.maxX, var6.maxY, var6.maxZ));
            } else {
                ExBlock.resetArea(var1, var6.minX, var6.minY, var6.minZ, var6.maxX, var6.maxY, var6.maxZ);
            }
        }

    }

    public void setTriggerToSelection(World var1, int var2, int var3, int var4) {
        AC_TileEntityMinMax var5 = (AC_TileEntityMinMax) var1.getBlockEntity(var2, var3, var4);
        var5.minX = AC_ItemCursor.minX;
        var5.minY = AC_ItemCursor.minY;
        var5.minZ = AC_ItemCursor.minZ;
        var5.maxX = AC_ItemCursor.maxX;
        var5.maxY = AC_ItemCursor.maxY;
        var5.maxZ = AC_ItemCursor.maxZ;
    }

    @Override
    public boolean canUse(World var1, int var2, int var3, int var4, PlayerEntity var5) {
        if (AC_DebugMode.active && var5.getHeldItem() != null && var5.getHeldItem().itemId == AC_Items.cursor.id) {
            AC_TileEntityTriggerPushable var6 = (AC_TileEntityTriggerPushable) var1.getBlockEntity(var2, var3, var4);
            AC_GuiTriggerPushable.showUI(var6);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void incrementColor(World var1, int var2, int var3, int var4) {
        AC_IBlockColor.super.incrementColor(var1, var2, var3, var4);
        this.onAdjacentBlockUpdate(var1, var2, var3, var4, 0);
    }
}
