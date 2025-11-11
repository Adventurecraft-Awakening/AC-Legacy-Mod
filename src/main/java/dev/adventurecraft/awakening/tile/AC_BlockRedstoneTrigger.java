package dev.adventurecraft.awakening.tile;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.common.AC_TriggerArea;
import dev.adventurecraft.awakening.common.gui.AC_GuiRedstoneTrigger;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.item.AC_ItemCursor;
import dev.adventurecraft.awakening.tile.entity.AC_TileEntityRedstoneTrigger;
import dev.adventurecraft.awakening.world.AC_LevelSource;
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
        var entity = ((AC_LevelSource) view).ac$getTileEntity(x, y, z, AC_TileEntityRedstoneTrigger.class);
        if (entity != null) {
            return entity.isActivated ? this.tex : this.tex + 1;
        }
        return this.tex;
    }

    private void updateBlock(Level world, int x, int y, int z, int side) {
        var entity = ((ExWorld) world).ac$getTileEntity(x, y, z, AC_TileEntityRedstoneTrigger.class);
        boolean isActivated = world.hasNeighborSignal(x, y, z);
        if (entity.isActivated == isActivated) {
            return;
        }
        entity.isActivated = isActivated;
        world.sendTileUpdated(x, y, z); // TODO: would this not be better at method tail?
        if (isActivated) {
            if (!entity.resetOnTrigger) {
                var area = new AC_TriggerArea(entity.min(), entity.max());
                ((ExWorld) world).getTriggerManager().addArea(x, y, z, area);
            }
            else {
                ExBlock.resetArea(world, entity.min(), entity.max());
            }
        }
        else {
            ((ExWorld) world).getTriggerManager().removeArea(x, y, z);
        }
    }

    @Override
    public boolean use(Level world, int x, int y, int z, Player player) {
        if (!AC_DebugMode.showDebugGuiOnUse(player)) {
            return false;
        }
        var entity = ((ExWorld) world).ac$getTileEntity(x, y, z, AC_TileEntityRedstoneTrigger.class);
        AC_GuiRedstoneTrigger.showUI(entity);
        return true;
    }

    public void setTriggerToSelection(Level world, int x, int y, int z) {
        var entity = ((ExWorld) world).ac$getTileEntity(x, y, z, AC_TileEntityRedstoneTrigger.class);
        entity.setMin(AC_ItemCursor.min());
        entity.setMax(AC_ItemCursor.max());
    }
}
