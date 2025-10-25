package dev.adventurecraft.awakening.tile;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.common.AC_TriggerArea;
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

        if (view.getTileEntity(x, y, z) instanceof AC_TileEntityRedstoneTrigger redStoneTrigger) {
            return redStoneTrigger.isActivated ? this.tex : this.tex + 1;
        }
        return this.tex;
    }

    private void updateBlock(Level world, int x, int y, int z, int side) {
        if (!(world.getTileEntity(x, y, z) instanceof AC_TileEntityRedstoneTrigger redStoneTrigger)) {
            return;
        }
        boolean isActivated = world.hasNeighborSignal(x, y, z);
        if (redStoneTrigger.isActivated != isActivated) {
            redStoneTrigger.isActivated = isActivated;
            world.sendTileUpdated(x, y, z);
            if (isActivated) {
                if (!redStoneTrigger.resetOnTrigger) {
                    var area = new AC_TriggerArea(redStoneTrigger.min(), redStoneTrigger.max());
                    ((ExWorld) world).getTriggerManager().addArea(x, y, z, area);
                }
                else {
                    ExBlock.resetArea(world, redStoneTrigger.min(), redStoneTrigger.max());
                }
            }
            else {
                ((ExWorld) world).getTriggerManager().removeArea(x, y, z);
            }
        }

    }

    @Override
    public boolean use(Level world, int x, int y, int z, Player player) {
        if (!AC_DebugMode.active) {
            return false;
        }
        if ((player.getSelectedItem() == null || player.getSelectedItem().id == AC_Items.cursor.id)) {
            if (world.getTileEntity(x, y, z) instanceof AC_TileEntityRedstoneTrigger redStoneTrigger) {
                AC_GuiRedstoneTrigger.showUI(redStoneTrigger);
                return true;
            }
        }
        return false;
    }

    public void setTriggerToSelection(Level world, int x, int y, int z) {
        if (!(world.getTileEntity(x, y, z) instanceof AC_TileEntityRedstoneTrigger redStoneTrigger)) {
            return;
        }
        redStoneTrigger.setMin(AC_ItemCursor.min());
        redStoneTrigger.setMax(AC_ItemCursor.max());
    }
}
