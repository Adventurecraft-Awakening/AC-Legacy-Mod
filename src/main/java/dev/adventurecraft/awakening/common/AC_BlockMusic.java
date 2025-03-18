package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.common.gui.AC_GuiMusic;
import dev.adventurecraft.awakening.extension.client.sound.ExSoundHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.TileEntityTile;
import net.minecraft.world.level.tile.entity.TileEntity;
import net.minecraft.world.phys.AABB;

public class AC_BlockMusic extends TileEntityTile implements AC_ITriggerBlock {

    protected AC_BlockMusic(int var1, int var2) {
        super(var1, var2, Material.AIR);
    }

    @Override
    protected TileEntity newTileEntity() {
        return new AC_TileEntityMusic();
    }

    @Override
    public boolean isSolidRender() {
        return false;
    }

    @Override
    public AABB getAABB(Level world, int x, int y, int z) {
        return null;
    }

    @Override
    public boolean shouldRender(LevelSource view, int x, int y, int z) {
        return AC_DebugMode.active;
    }

    @Override
    public boolean canBeTriggered() {
        return true;
    }

    @Override
    public void onTriggerActivated(Level world, int x, int y, int z) {
        var entity = (AC_TileEntityMusic) world.getTileEntity(x, y, z);
        var soundHelper = (ExSoundHelper) Minecraft.instance.soundEngine;
        if (!entity.musicName.equals("")) {
            soundHelper.playMusicFromStreaming(entity.musicName, entity.fadeOut, entity.fadeIn);
        } else {
            soundHelper.stopMusic();
        }
    }

    @Override
    public void onTriggerDeactivated(Level world, int x, int y, int z) {
    }

    @Override
    public boolean use(Level world, int x, int y, int z, Player player) {
        if (AC_DebugMode.active && (player.getSelectedItem() == null || player.getSelectedItem().id == AC_Items.cursor.id)) {
            var entity = (AC_TileEntityMusic) world.getTileEntity(x, y, z);
            AC_GuiMusic.showUI(world, entity);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean mayPick() {
        return AC_DebugMode.active;
    }
}
