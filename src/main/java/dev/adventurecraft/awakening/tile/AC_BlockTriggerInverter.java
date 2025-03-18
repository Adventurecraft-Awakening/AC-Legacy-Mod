package dev.adventurecraft.awakening.tile;

import java.util.Random;

import dev.adventurecraft.awakening.common.*;
import dev.adventurecraft.awakening.common.gui.AC_GuiTriggerInverter;
import dev.adventurecraft.awakening.tile.entity.AC_TileEntityTriggerInverter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.TileEntityTile;
import net.minecraft.world.level.tile.entity.TileEntity;
import net.minecraft.world.phys.AABB;
import dev.adventurecraft.awakening.extension.world.ExWorld;

public class AC_BlockTriggerInverter extends TileEntityTile implements AC_ITriggerBlock {

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
    public boolean shouldRender(LevelSource view, int x, int y, int z) {
        return AC_DebugMode.active;
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
    public boolean canBeTriggered() {
        return true;
    }

    @Override
    public void onTriggerActivated(Level world, int x, int y, int z) {
        ((ExWorld) world).getTriggerManager().removeArea(x, y, z);
    }

    @Override
    public void onTriggerDeactivated(Level world, int x, int y, int z) {
        var entity = (AC_TileEntityTriggerInverter) world.getTileEntity(x, y, z);
        ((ExWorld) world).getTriggerManager().addArea(x, y, z, new AC_TriggerArea(entity.minX, entity.minY, entity.minZ, entity.maxX, entity.maxY, entity.maxZ));
    }

    public void setTriggerToSelection(Level world, int x, int y, int z) {
        var entity = (AC_TileEntityTriggerInverter) world.getTileEntity(x, y, z);
        if (entity.minX != AC_ItemCursor.minX ||
            entity.minY != AC_ItemCursor.minY ||
            entity.minZ != AC_ItemCursor.minZ ||
            entity.maxX != AC_ItemCursor.maxX ||
            entity.maxY != AC_ItemCursor.maxY ||
            entity.maxZ != AC_ItemCursor.maxZ) {
            entity.set(AC_ItemCursor.minX, AC_ItemCursor.minY, AC_ItemCursor.minZ, AC_ItemCursor.maxX, AC_ItemCursor.maxY, AC_ItemCursor.maxZ);
        }
    }

    @Override
    public boolean use(Level world, int x, int y, int z, Player player) {
        if (AC_DebugMode.active && (player.getSelectedItem() == null || player.getSelectedItem().id == AC_Items.cursor.id)) {
            var entity = (AC_TileEntityTriggerInverter) world.getTileEntity(x, y, z);
            AC_GuiTriggerInverter.showUI(world, x, y, z, entity);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void reset(Level world, int x, int y, int z, boolean forDeath) {
        if (!((ExWorld) world).getTriggerManager().isActivated(x, y, z)) {
            this.onTriggerDeactivated(world, x, y, z);
        }
    }
}
