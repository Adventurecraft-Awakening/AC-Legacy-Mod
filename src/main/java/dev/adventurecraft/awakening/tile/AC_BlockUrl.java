package dev.adventurecraft.awakening.tile;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.common.gui.AC_GuiUrl;
import dev.adventurecraft.awakening.common.gui.AC_GuiUrlRequest;
import dev.adventurecraft.awakening.tile.entity.AC_TileEntityUrl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.TileEntityTile;
import net.minecraft.world.level.tile.entity.TileEntity;
import net.minecraft.world.phys.AABB;

public class AC_BlockUrl extends TileEntityTile implements AC_ITriggerDebugBlock {

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
    public void onTriggerActivated(Level world, int x, int y, int z) {
        if (!(world.getTileEntity(x, y, z) instanceof AC_TileEntityUrl entityUrl)) {
            return;
        }
        if (entityUrl.url != null && !entityUrl.url.isEmpty()) {
            AC_GuiUrlRequest.showUI(entityUrl.url);
        }
    }

    @Override
    public boolean use(Level world, int x, int y, int z, Player player) {
        if (!AC_DebugMode.active) {
            return false;
        }
        if (world.getTileEntity(x, y, z) instanceof AC_TileEntityUrl entityUrl) {
            AC_GuiUrl.showUI(entityUrl);
            return true;
        }
        return false;
    }

    @Override
    public boolean mayPick() {
        return AC_DebugMode.active;
    }
}