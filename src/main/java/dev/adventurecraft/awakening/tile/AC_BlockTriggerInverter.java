package dev.adventurecraft.awakening.tile;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.common.AC_TriggerArea;
import dev.adventurecraft.awakening.common.Coord;
import dev.adventurecraft.awakening.common.gui.AC_GuiTriggerInverter;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.item.AC_ItemCursor;
import dev.adventurecraft.awakening.item.AC_Items;
import dev.adventurecraft.awakening.tile.entity.AC_TileEntityTriggerInverter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.TileEntityTile;
import net.minecraft.world.level.tile.entity.TileEntity;
import net.minecraft.world.phys.AABB;

import java.util.Random;

public class AC_BlockTriggerInverter extends TileEntityTile implements AC_ITriggerDebugBlock {

    protected AC_BlockTriggerInverter(int var1, int var2) {
        super(var1, var2, Material.AIR);
    }

    @Override
    protected TileEntity newTileEntity() {
        return new AC_TileEntityTriggerInverter();
    }

    @Override
    public int getResource(int meta, Random rand) {
        return 0;
    }

    @Override
    public int getResourceCount(Random rand) {
        return 0;
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
    public int getTexture(LevelSource view, int x, int y, int z, int side) {
        return super.getTexture(view, x, y, z, side);
    }

    @Override
    public boolean mayPick() {
        return AC_DebugMode.active;
    }

    @Override
    public void onTriggerActivated(Level world, int x, int y, int z) {
        ((ExWorld) world).getTriggerManager().removeArea(x, y, z);
    }

    @Override
    public void onTriggerDeactivated(Level world, int x, int y, int z) {
        if (!(world.getTileEntity(x, y, z) instanceof AC_TileEntityTriggerInverter entityTriggerInverter)) {
            return;
        }
        var area = new AC_TriggerArea(entityTriggerInverter.min(), entityTriggerInverter.max());
        ((ExWorld) world).getTriggerManager().addArea(x, y, z, area);

    }

    public void setTriggerToSelection(Level world, int x, int y, int z) {
        if (!(world.getTileEntity(x, y, z) instanceof AC_TileEntityTriggerInverter entityTriggerInverter)) {
            return;
        }
        Coord min = AC_ItemCursor.min();
        Coord max = AC_ItemCursor.max();
        if (!entityTriggerInverter.min().equals(min) || !entityTriggerInverter.max().equals(max)) {
            entityTriggerInverter.set(min, max);
        }

    }

    @Override
    public boolean use(Level world, int x, int y, int z, Player player) {
        if (!AC_DebugMode.active) {
            return false;
        }
        if ((player.getSelectedItem() == null || player.getSelectedItem().id == AC_Items.cursor.id)) {
            if (world.getTileEntity(x, y, z) instanceof AC_TileEntityTriggerInverter entityTriggerInverter) {
                AC_GuiTriggerInverter.showUI(entityTriggerInverter);
                return true;
            }
        }
        return false;
    }

    @Override
    public void reset(Level world, int x, int y, int z, boolean forDeath) {
        if (!((ExWorld) world).getTriggerManager().isActivated(x, y, z)) {
            this.onTriggerDeactivated(world, x, y, z);
        }
    }
}
