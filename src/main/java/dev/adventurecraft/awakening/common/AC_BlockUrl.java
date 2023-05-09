package dev.adventurecraft.awakening.common;

import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.material.Material;
import net.minecraft.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class AC_BlockUrl extends BlockWithEntity implements AC_ITriggerBlock {

    protected AC_BlockUrl(int var1, int var2) {
        super(var1, var2, Material.AIR);
    }

    @Override
    protected BlockEntity createBlockEntity() {
        return new AC_TileEntityUrl();
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
        var entity = (AC_TileEntityUrl) world.getBlockEntity(x, y, z);
        if (entity.url != null && !entity.url.equals("")) {
            AC_GuiUrlRequest.showUI(entity.url);
        }
    }

    @Override
    public boolean canUse(World world, int x, int y, int z, PlayerEntity player) {
        if (AC_DebugMode.active) {
            var entity = (AC_TileEntityUrl) world.getBlockEntity(x, y, z);
            AC_GuiUrl.showUI(world, entity);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isCollidable() {
        return AC_DebugMode.active;
    }
}
