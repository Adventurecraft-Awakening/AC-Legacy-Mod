package dev.adventurecraft.awakening.common;

import java.util.List;

import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.material.Material;
import net.minecraft.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class AC_BlockHealDamage extends BlockWithEntity implements AC_ITriggerBlock {

    protected AC_BlockHealDamage(int var1, int var2) {
        super(var1, var2, Material.AIR);
    }

    protected BlockEntity createBlockEntity() {
        return new AC_TileEntityHealDamage();
    }

    @Override
    public boolean isFullOpaque() {
        return false;
    }

    @Override
    public AxixAlignedBoundingBox getCollisionShape(World world, int x, int y, int z) {
        return null;
    }

    @Override
    public boolean shouldRender(BlockView view, int x, int y, int z) {
        return AC_DebugMode.active;
    }

    @Override
    public boolean canBeTriggered() {
        return true;
    }

    @Override
    public void onTriggerActivated(World world, int x, int y, int z) {
        var entity = (AC_TileEntityHealDamage) world.getBlockEntity(x, y, z);

        for (PlayerEntity var8 : (List<PlayerEntity>) world.players) {
            if (entity.healDamage > 0) {
                var8.addHealth(entity.healDamage);
            } else {
                var8.applyDamage(-entity.healDamage);
            }
        }

    }

    @Override
    public void onTriggerDeactivated(World world, int x, int y, int z) {
    }

    @Override
    public boolean isCollidable() {
        return AC_DebugMode.active;
    }

    @Override
    public boolean canUse(World world, int x, int y, int z, PlayerEntity player) {
        if (AC_DebugMode.active && (player.getHeldItem() == null || player.getHeldItem().itemId == AC_Items.cursor.id)) {
            var entity = (AC_TileEntityHealDamage) world.getBlockEntity(x, y, z);
            AC_GuiHealDamage.showUI(world, entity);
            return true;
        } else {
            return false;
        }
    }
}
