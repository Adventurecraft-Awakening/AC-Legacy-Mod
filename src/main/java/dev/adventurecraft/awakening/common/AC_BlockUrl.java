package dev.adventurecraft.awakening.common;

import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.material.Material;
import net.minecraft.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class AC_BlockUrl extends BlockWithEntity {
    protected AC_BlockUrl(int var1, int var2) {
        super(var1, var2, Material.AIR);
    }

    protected BlockEntity createBlockEntity() {
        return new AC_TileEntityUrl();
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

    public void onTriggerActivated(World var1, int var2, int var3, int var4) {
        AC_TileEntityUrl var5 = (AC_TileEntityUrl) var1.getBlockEntity(var2, var3, var4);
        if (var5.url != null && !var5.url.equals("")) {
            AC_GuiUrlRequest.showUI(var5.url);
        }
    }

    public boolean canUse(World var1, int var2, int var3, int var4, PlayerEntity var5) {
        if (AC_DebugMode.active) {
            AC_TileEntityUrl var6 = (AC_TileEntityUrl) var1.getBlockEntity(var2, var3, var4);
            AC_GuiUrl.showUI(var1, var6);
            return true;
        } else {
            return false;
        }
    }

    public boolean isCollidable() {
        return AC_DebugMode.active;
    }
}
