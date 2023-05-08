package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.material.Material;
import net.minecraft.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class AC_BlockScript extends BlockWithEntity {

    protected AC_BlockScript(int var1, int var2) {
        super(var1, var2, Material.AIR);
    }

    @Override
    protected BlockEntity createBlockEntity() {
        return new AC_TileEntityScript();
    }

    @Override
    public boolean isFullOpaque() {
        return false;
    }

    @Override
    public AxixAlignedBoundingBox getCollisionShape(World world, int x, int y, int z) {
        return null;
    }

    public boolean shouldRender(BlockView view, int x, int y, int z) {
        return AC_DebugMode.active;
    }

    @Override
    public int getTextureForSide(BlockView view, int x, int y, int z, int side) {
        return super.getTextureForSide(view, x, y, z, side);
    }

    @Override
    public boolean isCollidable() {
        return AC_DebugMode.active;
    }

    public boolean canBeTriggered() {
        return true;
    }

    public void onTriggerActivated(World world, int x, int y, int z) {
        var entity = (AC_TileEntityScript) world.getBlockEntity(x, y, z);
        if (!entity.onTriggerScriptFile.equals("")) {
            ((ExWorld) world).getScriptHandler().runScript(entity.onTriggerScriptFile, entity.scope);
        }

        entity.isActivated = true;
    }

    public void onTriggerDeactivated(World world, int x, int y, int z) {
        var entity = (AC_TileEntityScript) world.getBlockEntity(x, y, z);
        if (!entity.onDetriggerScriptFile.equals("")) {
            ((ExWorld) world).getScriptHandler().runScript(entity.onDetriggerScriptFile, entity.scope);
        }

        entity.isActivated = false;
    }

    @Override
    public boolean canUse(World world, int x, int y, int z, PlayerEntity player) {
        if (AC_DebugMode.active) {
            var entity = (AC_TileEntityScript) world.getBlockEntity(x, y, z);
            AC_GuiScript.showUI(entity);
        }
        return true;
    }
}
