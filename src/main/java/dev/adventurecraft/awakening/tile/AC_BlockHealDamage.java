package dev.adventurecraft.awakening.tile;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.common.gui.AC_GuiHealDamage;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.tile.entity.AC_TileEntityHealDamage;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.TileEntityTile;
import net.minecraft.world.level.tile.entity.TileEntity;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class AC_BlockHealDamage extends TileEntityTile implements AC_ITriggerDebugBlock {

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
    public void onTriggerActivated(Level world, int x, int y, int z) {
        var entity = ((ExWorld) world).ac$getTileEntity(x, y, z, AC_TileEntityHealDamage.class);
        for (Player player : (List<Player>) world.players) {
            if (entity.healDamage > 0) {
                player.heal(entity.healDamage);
            }
            else {
                player.actuallyHurt(-entity.healDamage);
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
        if (!AC_DebugMode.showDebugGuiOnUse(player)) {
            return false;
        }
        var entity = ((ExWorld) world).ac$getTileEntity(x, y, z, AC_TileEntityHealDamage.class);
        AC_GuiHealDamage.showUI(entity);
        return true;
    }
}
