package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.material.Material;
import net.minecraft.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class AC_BlockTimer extends BlockWithEntity {
    protected AC_BlockTimer(int var1, int var2) {
        super(var1, var2, Material.AIR);
    }

    protected BlockEntity createBlockEntity() {
        return new AC_TileEntityTimer();
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

    public boolean canBeTriggered() {
        return true;
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
        }
    }

    public void onTriggerActivated(World var1, int var2, int var3, int var4) {
        AC_TileEntityTimer var5 = (AC_TileEntityTimer) var1.getBlockEntity(var2, var3, var4);
        if (var5.canActivate && !var5.active) {
            var5.startActive();
        }

    }

    public boolean canUse(World var1, int var2, int var3, int var4, PlayerEntity var5) {
        if (AC_DebugMode.active) {
            AC_TileEntityTimer var6 = (AC_TileEntityTimer) var1.getBlockEntity(var2, var3, var4);
            AC_GuiTimer.showUI(var1, var2, var3, var4, var6);
        }

        return true;
    }

    public boolean isCollidable() {
        return AC_DebugMode.active;
    }

    public void reset(World var1, int var2, int var3, int var4, boolean var5) {
        AC_TileEntityTimer var6 = (AC_TileEntityTimer) var1.getBlockEntity(var2, var3, var4);
        var6.active = false;
        var6.canActivate = true;
        var6.ticks = 0;
        ((ExWorld) var1).getTriggerManager().removeArea(var2, var3, var4);
    }
}
