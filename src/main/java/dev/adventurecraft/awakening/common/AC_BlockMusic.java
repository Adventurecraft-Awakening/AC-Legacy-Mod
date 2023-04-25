package dev.adventurecraft.awakening.common;

import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class AC_BlockMusic extends BlockWithEntity {
    protected AC_BlockMusic(int var1, int var2) {
        super(var1, var2, Material.AIR);
    }

    protected BlockEntity createBlockEntity() {
        return new AC_TileEntityMusic();
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
        AC_TileEntityMusic var5 = (AC_TileEntityMusic) var1.getBlockEntity(var2, var3, var4);
        if (!var5.musicName.equals("")) {
            //Minecraft.instance.soundHelper.playMusicFromStreaming(var5.musicName, var5.fadeOut, var5.fadeIn); TODO
        } else {
            //Minecraft.instance.soundHelper.stopMusic(); TODO
        }
    }

    public void onTriggerDeactivated(World var1, int var2, int var3, int var4) {
    }

    public boolean canUse(World var1, int var2, int var3, int var4, PlayerEntity var5) {
        if (AC_DebugMode.active && var5.getHeldItem() != null && var5.getHeldItem().itemId == AC_Items.cursor.id) {
            AC_TileEntityMusic var6 = (AC_TileEntityMusic) var1.getBlockEntity(var2, var3, var4);
            AC_GuiMusic.showUI(var1, var6);
            return true;
        } else {
            return false;
        }
    }

    public boolean isCollidable() {
        return AC_DebugMode.active;
    }
}
