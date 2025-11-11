package dev.adventurecraft.awakening.tile;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.common.gui.AC_GuiStorage;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.tile.entity.AC_TileEntityStorage;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.TileEntityTile;
import net.minecraft.world.level.tile.entity.TileEntity;
import net.minecraft.world.phys.AABB;

public class AC_BlockStorage extends TileEntityTile implements AC_ITriggerDebugBlock {

    protected AC_BlockStorage(int id, int texture) {
        super(id, texture, Material.AIR);
    }

    @Override
    protected TileEntity newTileEntity() {
        return new AC_TileEntityStorage();
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
        var entity = ((ExWorld) world).ac$getTileEntity(x, y, z, AC_TileEntityStorage.class);
        entity.loadCurrentArea();
    }

    @Override
    public boolean use(Level world, int x, int y, int z, Player player) {
        if (!AC_DebugMode.showDebugGuiOnUse(player)) {
            return false;
        }
        var entity = ((ExWorld) world).ac$getTileEntity(x, y, z, AC_TileEntityStorage.class);
        AC_GuiStorage.showUI(entity);
        return true;
    }

    @Override
    public boolean mayPick() {
        return AC_DebugMode.active;
    }
}
