package dev.adventurecraft.awakening.tile;

import dev.adventurecraft.awakening.common.*;
import dev.adventurecraft.awakening.common.gui.AC_GuiRedstoneTrigger;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.item.AC_ItemCursor;
import dev.adventurecraft.awakening.item.AC_Items;
import dev.adventurecraft.awakening.tile.entity.AC_TileEntityRedstoneTrigger;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.TileEntityTile;
import net.minecraft.world.level.tile.entity.TileEntity;

public class AC_BlockRedstoneTrigger extends TileEntityTile implements AC_ITriggerBlock {

    protected AC_BlockRedstoneTrigger(int var1, int var2) {
        super(var1, var2, Material.STONE);
    }

    @Override
    protected TileEntity newTileEntity() {
        return new AC_TileEntityRedstoneTrigger();
    }

    @Override
    public void neighborChanged(Level world, int x, int y, int z, int id) {
        this.updateBlock(world, x, y, z, id);
    }

    @Override
    public int getTexture(LevelSource view, int x, int y, int z, int side) {
        var entity = (AC_TileEntityRedstoneTrigger) view.getTileEntity(x, y, z);
        return entity.isActivated ? this.tex : this.tex + 1;
    }

    private void updateBlock(Level world, int x, int y, int z, int side) {
        boolean isActivated = world.hasNeighborSignal(x, y, z);
        var e = (AC_TileEntityRedstoneTrigger) world.getTileEntity(x, y, z);
        if (e != null && e.isActivated != isActivated) {
            e.isActivated = isActivated;
            world.sendTileUpdated(x, y, z);
            if (isActivated) {
                if (!e.resetOnTrigger) {
                    ((ExWorld) world).getTriggerManager().addArea(
                        x, y, z, new AC_TriggerArea(e.minX, e.minY, e.minZ, e.maxX, e.maxY, e.maxZ));
                } else {
                    ExBlock.resetArea(world, e.minX, e.minY, e.minZ, e.maxX, e.maxY, e.maxZ);
                }
            } else {
                ((ExWorld) world).getTriggerManager().removeArea(x, y, z);
            }
        }
    }

    @Override
    public boolean use(Level world, int x, int y, int z, Player player) {
        if (AC_DebugMode.active && (player.getSelectedItem() == null || player.getSelectedItem().id == AC_Items.cursor.id)) {
            var entity = (AC_TileEntityRedstoneTrigger) world.getTileEntity(x, y, z);
            AC_GuiRedstoneTrigger.showUI(entity);
            return true;
        } else {
            return false;
        }
    }

    public void setTriggerToSelection(Level world, int x, int y, int z) {
        var e = (AC_TileEntityRedstoneTrigger) world.getTileEntity(x, y, z);
        if (e.minX != AC_ItemCursor.minX ||
            e.minY != AC_ItemCursor.minY ||
            e.minZ != AC_ItemCursor.minZ ||
            e.maxX != AC_ItemCursor.maxX ||
            e.maxY != AC_ItemCursor.maxY ||
            e.maxZ != AC_ItemCursor.maxZ) {

            e.minX = AC_ItemCursor.minX;
            e.minY = AC_ItemCursor.minY;
            e.minZ = AC_ItemCursor.minZ;
            e.maxX = AC_ItemCursor.maxX;
            e.maxY = AC_ItemCursor.maxY;
            e.maxZ = AC_ItemCursor.maxZ;
        }
    }
}
