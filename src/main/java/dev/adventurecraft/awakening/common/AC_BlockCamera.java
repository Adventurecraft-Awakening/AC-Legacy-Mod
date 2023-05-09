package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class AC_BlockCamera extends BlockWithEntity implements AC_ITriggerBlock {

    protected AC_BlockCamera(int var1, int var2) {
        super(var1, var2, Material.AIR);
    }

    @Override
    protected BlockEntity createBlockEntity() {
        return new AC_TileEntityCamera();
    }

    @Override
    public boolean isFullOpaque() {
        return false;
    }

    @Override
    public AxixAlignedBoundingBox getCollisionShape(World var1, int var2, int var3, int var4) {
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
        var entity = (AC_TileEntityCamera) world.getBlockEntity(x, y, z);
        entity.loadCamera();
        ExMinecraft mc = (ExMinecraft) Minecraft.instance;
        mc.getCutsceneCamera().startCamera();
        mc.setCameraActive(true);
        mc.setCameraPause(entity.pauseGame);
    }

    @Override
    public void onTriggerDeactivated(World world, int x, int y, int z) {
    }

    @Override
    public boolean canUse(World world, int x, int y, int z, PlayerEntity player) {
        if (!AC_DebugMode.active) {
            return false;
        }

        Minecraft.instance.overlay.addChatMessage("Set Active Editing Camera");
        var entity = (AC_TileEntityCamera) world.getBlockEntity(x, y, z);
        ((ExMinecraft) Minecraft.instance).setActiveCutsceneCamera(entity.camera);
        entity.camera.loadCameraEntities();
        AC_GuiCameraBlock.showUI(entity);
        return true;
    }

    @Override
    public boolean isCollidable() {
        return AC_DebugMode.active;
    }
}
