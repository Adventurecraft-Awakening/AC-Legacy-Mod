package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.world.ExWorld;
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
        var entity = (AC_TileEntityRedstoneTrigger) world.getTileEntity(x, y, z);
        if (entity != null && entity.isActivated != isActivated) {
            entity.isActivated = isActivated;
            world.sendTileUpdated(x, y, z);
            if (isActivated) {
                if (!entity.resetOnTrigger) {
                    ((ExWorld) world).getTriggerManager().addArea(x, y, z, new AC_TriggerArea(entity.minX, entity.minY, entity.minZ, entity.maxX, entity.maxY, entity.maxZ));
                } else {
                    ExBlock.resetArea(world, entity.minX, entity.minY, entity.minZ, entity.maxX, entity.maxY, entity.maxZ);
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
            AC_GuiRedstoneTrigger.showUI(world, x, y, z, entity);
            return true;
        } else {
            return false;
        }
    }

    public void setTriggerToSelection(Level world, int x, int y, int z) {
        var entity = (AC_TileEntityRedstoneTrigger) world.getTileEntity(x, y, z);
        if (entity.minX != AC_ItemCursor.minX ||
            entity.minY != AC_ItemCursor.minY ||
            entity.minZ != AC_ItemCursor.minZ ||
            entity.maxX != AC_ItemCursor.maxX ||
            entity.maxY != AC_ItemCursor.maxY ||
            entity.maxZ != AC_ItemCursor.maxZ) {

            entity.minX = AC_ItemCursor.minX;
            entity.minY = AC_ItemCursor.minY;
            entity.minZ = AC_ItemCursor.minZ;
            entity.maxX = AC_ItemCursor.maxX;
            entity.maxY = AC_ItemCursor.maxY;
            entity.maxZ = AC_ItemCursor.maxZ;
        }
    }
}
