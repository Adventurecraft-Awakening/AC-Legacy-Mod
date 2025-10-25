package dev.adventurecraft.awakening.tile;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.common.gui.AC_GuiStorage;
import dev.adventurecraft.awakening.item.AC_Items;
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
        if (!(world.getTileEntity(x, y, z) instanceof AC_TileEntityStorage entityStorage)) {
            return;
        }
        entityStorage.loadCurrentArea();

    }

    @Override
    public boolean use(Level world, int x, int y, int z, Player player) {
        if (!AC_DebugMode.active) {
            return false;
        }
        if ((player.getSelectedItem() == null || player.getSelectedItem().id == AC_Items.cursor.id)) {
            if (world.getTileEntity(x, y, z) instanceof AC_TileEntityStorage entityStorage) {
                AC_GuiStorage.showUI(entityStorage);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mayPick() {
        return AC_DebugMode.active;
    }
}
