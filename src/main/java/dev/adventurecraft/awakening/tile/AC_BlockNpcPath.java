package dev.adventurecraft.awakening.tile;

import dev.adventurecraft.awakening.extension.entity.player.ExPlayerEntity;
import dev.adventurecraft.awakening.item.AC_Items;
import dev.adventurecraft.awakening.tile.entity.AC_TileEntityNpcPath;
import dev.adventurecraft.awakening.common.gui.AC_GuiNpcPath;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.TileEntityTile;
import net.minecraft.world.level.tile.entity.TileEntity;
import net.minecraft.world.phys.AABB;

public class AC_BlockNpcPath extends TileEntityTile implements AC_ITriggerDebugBlock {

    public AC_BlockNpcPath(int var1, int var2) {
        super(var1, var2, Material.STONE);
    }

    @Override
    protected TileEntity newTileEntity() {
        return new AC_TileEntityNpcPath();
    }

    @Override
    public AABB getAABB(Level var1, int var2, int var3, int var4) {
        return null;
    }

    @Override
    public boolean isSolidRender() {
        return false;
    }

    @Override
    public boolean mayPick() {
        return false;
    }

    @Override
    public void onTriggerActivated(Level world, int x, int y, int z) {
        var entity = (AC_TileEntityNpcPath) world.getTileEntity(x, y, z);
        if (entity != null) {
            entity.pathEntity();
        }
    }

    @Override
    public boolean use(Level world, int x, int y, int z, Player player) {
        if (!((ExPlayerEntity) player).isDebugMode()) {
            return false;
        }
        ItemInstance item = player.getSelectedItem();
        if (item == null || item.id == AC_Items.cursor.id) {
            var entity = (AC_TileEntityNpcPath) world.getTileEntity(x, y, z);
            if (entity != null) {
                AC_GuiNpcPath.showUI(entity);
            }
            return true;
        }
        return false;
    }
}
