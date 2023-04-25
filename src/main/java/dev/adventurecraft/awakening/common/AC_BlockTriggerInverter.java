package dev.adventurecraft.awakening.common;

import java.util.Random;

import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.material.Material;
import net.minecraft.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class AC_BlockTriggerInverter extends BlockWithEntity {
    protected AC_BlockTriggerInverter(int var1, int var2) {
        super(var1, var2, Material.AIR);
    }

    protected BlockEntity createBlockEntity() {
        return new AC_TileEntityTriggerInverter();
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

    public boolean canBeTriggered() {
        return true;
    }

    public void onTriggerActivated(World var1, int var2, int var3, int var4) {
        ((ExWorld) var1).getTriggerManager().removeArea(var2, var3, var4);
    }

    public void onTriggerDeactivated(World var1, int var2, int var3, int var4) {
        AC_TileEntityTriggerInverter var5 = (AC_TileEntityTriggerInverter) var1.getBlockEntity(var2, var3, var4);
        ((ExWorld) var1).getTriggerManager().addArea(var2, var3, var4, new AC_TriggerArea(var5.minX, var5.minY, var5.minZ, var5.maxX, var5.maxY, var5.maxZ));
    }

    public void setTriggerToSelection(World var1, int var2, int var3, int var4) {
        AC_TileEntityTriggerInverter var5 = (AC_TileEntityTriggerInverter) var1.getBlockEntity(var2, var3, var4);
        if (var5.minX != AC_ItemCursor.minX || var5.minY != AC_ItemCursor.minY || var5.minZ != AC_ItemCursor.minZ || var5.maxX != AC_ItemCursor.maxX || var5.maxY != AC_ItemCursor.maxY || var5.maxZ != AC_ItemCursor.maxZ) {
            var5.set(AC_ItemCursor.minX, AC_ItemCursor.minY, AC_ItemCursor.minZ, AC_ItemCursor.maxX, AC_ItemCursor.maxY, AC_ItemCursor.maxZ);
        }
    }

    public boolean canUse(World var1, int var2, int var3, int var4, PlayerEntity var5) {
        if (AC_DebugMode.active && var5.getHeldItem() != null && var5.getHeldItem().itemId == AC_Items.cursor.id) {
            AC_TileEntityTriggerInverter var6 = (AC_TileEntityTriggerInverter) var1.getBlockEntity(var2, var3, var4);
            AC_GuiTriggerInverter.showUI(var1, var2, var3, var4, var6);
            return true;
        } else {
            return false;
        }
    }

    public void reset(World var1, int var2, int var3, int var4, boolean var5) {
        if (!((ExWorld) var1).getTriggerManager().isActivated(var2, var3, var4)) {
            this.onTriggerDeactivated(var1, var2, var3, var4);
        }
    }
}
