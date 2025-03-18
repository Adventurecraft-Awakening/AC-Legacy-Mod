package dev.adventurecraft.awakening.tile;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.tile.entity.AC_TileEntityUrl;
import dev.adventurecraft.awakening.common.gui.AC_GuiUrl;
import dev.adventurecraft.awakening.common.gui.AC_GuiUrlRequest;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.TileEntityTile;
import net.minecraft.world.level.tile.entity.TileEntity;
import net.minecraft.world.phys.AABB;

public class AC_BlockUrl extends TileEntityTile implements AC_ITriggerBlock {

    protected AC_BlockUrl(int id, int tex) {
        super(id, tex, Material.AIR);
    }

    @Override
    protected TileEntity newTileEntity() {
        return new AC_TileEntityUrl();
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
        var entity = (AC_TileEntityUrl) world.getTileEntity(x, y, z);
        if (entity.url != null && !entity.url.isEmpty()) {
            AC_GuiUrlRequest.showUI(entity.url);
        }
    }

    @Override
    public boolean use(Level world, int x, int y, int z, Player player) {
        if (AC_DebugMode.active) {
            var entity = (AC_TileEntityUrl) world.getTileEntity(x, y, z);
            AC_GuiUrl.showUI(entity);
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
