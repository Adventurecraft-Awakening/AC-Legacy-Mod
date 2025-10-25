package dev.adventurecraft.awakening.tile;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.common.AC_TriggerArea;
import dev.adventurecraft.awakening.common.gui.AC_GuiTriggerPushable;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.item.AC_ItemCursor;
import dev.adventurecraft.awakening.item.AC_Items;
import dev.adventurecraft.awakening.tile.entity.AC_TileEntityMinMax;
import dev.adventurecraft.awakening.tile.entity.AC_TileEntityTriggerPushable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.entity.TileEntity;

public class AC_BlockTriggerPushable extends AC_BlockColorWithEntity {

    protected AC_BlockTriggerPushable(int var1, int var2) {
        super(var1, var2, Material.STONE);
    }

    @Override
    protected TileEntity newTileEntity() {
        return new AC_TileEntityTriggerPushable();
    }

    private boolean checkBlock(Level world, int x, int y, int z, int meta) {
        return world.getTile(x, y, z) == AC_Blocks.pushableBlock.id && world.getData(x, y, z) == meta;
    }

    @Override
    public void neighborChanged(Level world, int x, int y, int z, int id) {
        if (!(world.getTileEntity(x, y, z) instanceof AC_TileEntityTriggerPushable entityTriggerPushable)) {

            return;
        }
        int meta = world.getData(x, y, z);
        boolean pushable = this.checkBlock(world, x + 1, y, z, meta);
        pushable |= this.checkBlock(world, x - 1, y, z, meta);
        pushable |= this.checkBlock(world, x, y + 1, z, meta);
        pushable |= this.checkBlock(world, x, y - 1, z, meta);
        pushable |= this.checkBlock(world, x, y, z + 1, meta);
        pushable |= this.checkBlock(world, x, y, z - 1, meta);
        if (entityTriggerPushable.activated) {
            if (!pushable) {
                entityTriggerPushable.activated = false;
                ((ExWorld) world).getTriggerManager().removeArea(x, y, z);
            }
        }
        else if (pushable) {
            entityTriggerPushable.activated = true;
            if (!entityTriggerPushable.resetOnTrigger) {
                var area = new AC_TriggerArea(entityTriggerPushable.min(), entityTriggerPushable.max());
                ((ExWorld) world).getTriggerManager().addArea(x, y, z, area);
            }
            else {
                ExBlock.resetArea(world, entityTriggerPushable.min(), entityTriggerPushable.max());
            }
        }
    }
    public void setTriggerToSelection(Level world, int x, int y, int z) {
        if (!(world.getTileEntity(x, y, z) instanceof AC_TileEntityMinMax entityMinMax)) {
            return;
        }
        entityMinMax.setMin(AC_ItemCursor.min());
        entityMinMax.setMax(AC_ItemCursor.max());

    }

    @Override
    public boolean use(Level world, int x, int y, int z, Player player) {
        if (!AC_DebugMode.active) {
            return false;
        }
        if ((player.getSelectedItem() == null || player.getSelectedItem().id == AC_Items.cursor.id)) {
            if (world.getTileEntity(x, y, z) instanceof AC_TileEntityTriggerPushable entityTriggerPushable) {
                AC_GuiTriggerPushable.showUI(entityTriggerPushable);
                return true;
            }
        }
        return false;
    }

    @Override
    public void incrementColor(Level world, int x, int y, int z, int amount) {
        super.incrementColor(world, x, y, z, amount);
        this.neighborChanged(world, x, y, z, 0);
    }
}
