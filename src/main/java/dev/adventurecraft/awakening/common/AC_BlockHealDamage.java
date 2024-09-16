package dev.adventurecraft.awakening.common;

import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.TileEntityTile;
import net.minecraft.world.level.tile.entity.TileEntity;
import net.minecraft.world.phys.AABB;

public class AC_BlockHealDamage extends TileEntityTile implements AC_ITriggerBlock {

    protected AC_BlockHealDamage(int var1, int var2) {
        super(var1, var2, Material.AIR);
    }

    protected TileEntity newTileEntity() {
        return new AC_TileEntityHealDamage();
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
    public boolean canBeTriggered() {
        return true;
    }

    @Override
    public void onTriggerActivated(Level world, int x, int y, int z) {
        var entity = (AC_TileEntityHealDamage) world.getTileEntity(x, y, z);

        for (Player var8 : (List<Player>) world.players) {
            if (entity.healDamage > 0) {
                var8.heal(entity.healDamage);
            } else {
                var8.actuallyHurt(-entity.healDamage);
            }
        }

    }

    @Override
    public void onTriggerDeactivated(Level world, int x, int y, int z) {
    }

    @Override
    public boolean mayPick() {
        return AC_DebugMode.active;
    }

    @Override
    public boolean use(Level world, int x, int y, int z, Player player) {
        if (AC_DebugMode.active && (player.getSelectedItem() == null || player.getSelectedItem().id == AC_Items.cursor.id)) {
            var entity = (AC_TileEntityHealDamage) world.getTileEntity(x, y, z);
            AC_GuiHealDamage.showUI(world, entity);
            return true;
        } else {
            return false;
        }
    }
}
