package dev.adventurecraft.awakening.tile;

import java.util.Random;

import dev.adventurecraft.awakening.common.*;
import dev.adventurecraft.awakening.common.gui.AC_GuiTriggerInverter;
import dev.adventurecraft.awakening.item.AC_ItemCursor;
import dev.adventurecraft.awakening.item.AC_Items;
import dev.adventurecraft.awakening.tile.entity.AC_TileEntityTriggerInverter;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.TileEntityTile;
import net.minecraft.world.level.tile.entity.TileEntity;
import net.minecraft.world.phys.AABB;
import dev.adventurecraft.awakening.extension.world.ExWorld;

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
        var entity = (AC_TileEntityTriggerInverter) world.getTileEntity(x, y, z);
        var area = new AC_TriggerArea(entity.min(), entity.max());
        ((ExWorld) world).getTriggerManager().addArea(x, y, z, area);
    }

    public void setTriggerToSelection(Level world, int x, int y, int z) {
        var entity = (AC_TileEntityTriggerInverter) world.getTileEntity(x, y, z);
        Coord min = AC_ItemCursor.min();
        Coord max = AC_ItemCursor.max();
        if (!entity.min().equals(min) || !entity.max().equals(max)) {
            entity.set(min, max);
        }
    }

    @Override
    public boolean use(Level world, int x, int y, int z, Player player) {
        if (AC_DebugMode.active) {
            ItemInstance item = player.getSelectedItem();
            if (item == null || item.id == AC_Items.cursor.id) {
                var entity = (AC_TileEntityTriggerInverter) world.getTileEntity(x, y, z);
                AC_GuiTriggerInverter.showUI(entity);
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
