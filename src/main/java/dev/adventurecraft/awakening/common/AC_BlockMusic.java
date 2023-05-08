package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.client.sound.ExSoundHelper;
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

    @Override
    protected BlockEntity createBlockEntity() {
        return new AC_TileEntityMusic();
    }

    @Override
    public boolean isFullOpaque() {
        return false;
    }

    @Override
    public AxixAlignedBoundingBox getCollisionShape(World world, int x, int y, int z) {
        return null;
    }

    public boolean shouldRender(BlockView view, int x, int y, int z) {
        return AC_DebugMode.active;
    }

    public boolean canBeTriggered() {
        return true;
    }

    public void onTriggerActivated(World world, int x, int y, int z) {
        var tileEntity = (AC_TileEntityMusic) world.getBlockEntity(x, y, z);
        var soundHelper = (ExSoundHelper) Minecraft.instance.soundHelper;
        if (!tileEntity.musicName.equals("")) {
            soundHelper.playMusicFromStreaming(tileEntity.musicName, tileEntity.fadeOut, tileEntity.fadeIn);
        } else {
            soundHelper.stopMusic();
        }
    }

    public void onTriggerDeactivated(World world, int x, int y, int z) {
    }

    @Override
    public boolean canUse(World world, int x, int y, int z, PlayerEntity player) {
        if (AC_DebugMode.active && player.getHeldItem() != null && player.getHeldItem().itemId == AC_Items.cursor.id) {
            var tileEntity = (AC_TileEntityMusic) world.getBlockEntity(x, y, z);
            AC_GuiMusic.showUI(world, tileEntity);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isCollidable() {
        return AC_DebugMode.active;
    }
}
