package dev.adventurecraft.awakening.tile;

import dev.adventurecraft.awakening.common.*;
import dev.adventurecraft.awakening.common.gui.AC_GuiTimer;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.item.AC_ItemCursor;
import dev.adventurecraft.awakening.tile.entity.AC_TileEntityMinMax;
import dev.adventurecraft.awakening.tile.entity.AC_TileEntityTimer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.TileEntityTile;
import net.minecraft.world.level.tile.entity.TileEntity;
import net.minecraft.world.phys.AABB;

public class AC_BlockTimer extends TileEntityTile implements AC_ITriggerDebugBlock {

    protected AC_BlockTimer(int var1, int var2) {
        super(var1, var2, Material.AIR);
    }

    @Override
    protected TileEntity newTileEntity() {
        return new AC_TileEntityTimer();
    }

    @Override
    public boolean isSolidRender() {
        return false;
    }

    @Override
    public AABB getAABB(Level world, int x, int y, int z) {
        return null;
    }

    public void setTriggerToSelection(Level world, int x, int y, int z) {
        var entity = (AC_TileEntityMinMax) world.getTileEntity(x, y, z);
        entity.setMin(AC_ItemCursor.min());
        entity.setMax(AC_ItemCursor.max());
    }

    @Override
    public void onTriggerActivated(Level world, int x, int y, int z) {
        var entity = (AC_TileEntityTimer) world.getTileEntity(x, y, z);
        if (entity.canActivate && !entity.active) {
            entity.startActive();
        }
    }

    @Override
    public boolean use(Level world, int x, int y, int z, Player player) {
        if (AC_DebugMode.active) {
            var entity = (AC_TileEntityTimer) world.getTileEntity(x, y, z);
            AC_GuiTimer.showUI(entity);
        }
        return true;
    }

    @Override
    public boolean mayPick() {
        return AC_DebugMode.active;
    }

    @Override
    public void reset(Level world, int x, int y, int z, boolean forDeath) {
        var entity = (AC_TileEntityTimer) world.getTileEntity(x, y, z);
        entity.active = false;
        entity.canActivate = true;
        entity.ticks = 0;
        ((ExWorld) world).getTriggerManager().removeArea(x, y, z);
    }
}
