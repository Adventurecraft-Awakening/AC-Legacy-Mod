package dev.adventurecraft.awakening.tile;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.common.AC_TriggerArea;
import dev.adventurecraft.awakening.common.gui.AC_GuiTriggerPushable;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.world.ExWorld;
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
        var entity = ((ExWorld) world).ac$getTileEntity(x, y, z, AC_TileEntityTriggerPushable.class);
        int meta = world.getData(x, y, z);
        boolean pushable = this.checkBlock(world, x + 1, y, z, meta);
        pushable = pushable || this.checkBlock(world, x - 1, y, z, meta);
        pushable = pushable || this.checkBlock(world, x, y + 1, z, meta);
        pushable = pushable || this.checkBlock(world, x, y - 1, z, meta);
        pushable = pushable || this.checkBlock(world, x, y, z + 1, meta);
        pushable = pushable || this.checkBlock(world, x, y, z - 1, meta);
        if (entity.activated) {
            if (!pushable) {
                entity.activated = false;
                ((ExWorld) world).getTriggerManager().removeArea(x, y, z);
            }
        }
        else if (pushable) {
            entity.activated = true;
            if (!entity.resetOnTrigger) {
                var area = new AC_TriggerArea(entity.min(), entity.max());
                ((ExWorld) world).getTriggerManager().addArea(x, y, z, area);
            }
            else {
                ExBlock.resetArea(world, entity.min(), entity.max());
            }
        }
    }

    @Override
    public boolean use(Level world, int x, int y, int z, Player player) {
        if (!AC_DebugMode.showDebugGuiOnUse(player)) {
            return false;
        }
        var entity = ((ExWorld) world).ac$getTileEntity(x, y, z, AC_TileEntityTriggerPushable.class);
        AC_GuiTriggerPushable.showUI(entity);
        return true;
    }

    @Override
    public void incrementColor(Level world, int x, int y, int z, int amount) {
        super.incrementColor(world, x, y, z, amount);
        this.neighborChanged(world, x, y, z, 0);
    }
}
