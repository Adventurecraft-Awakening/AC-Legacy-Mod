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

public class AC_BlockCamera extends BlockWithEntity {
    protected AC_BlockCamera(int var1, int var2) {
        super(var1, var2, Material.AIR);
    }

    protected BlockEntity createBlockEntity() {
        return new AC_TileEntityCamera();
    }

    public boolean isFullOpaque() {
        return false;
    }

    public AxixAlignedBoundingBox getCollisionShape(World var1, int var2, int var3, int var4) {
        return null;
    }

    public boolean shouldRender(BlockView var1, int var2, int var3, int var4) {
        return AC_DebugMode.active;
    }

    public boolean canBeTriggered() {
        return true;
    }

    public void onTriggerActivated(World var1, int var2, int var3, int var4) {
        AC_TileEntityCamera var5 = (AC_TileEntityCamera) var1.getBlockEntity(var2, var3, var4);
        var5.loadCamera();
        ExMinecraft mc = (ExMinecraft) Minecraft.instance;
        mc.getCutsceneCamera().startCamera();
        mc.setCameraActive(true);
        mc.setCameraPause(var5.pauseGame);
    }

    public void onTriggerDeactivated(World var1, int var2, int var3, int var4) {
    }

    public boolean canUse(World var1, int var2, int var3, int var4, PlayerEntity var5) {
        if (AC_DebugMode.active) {
            Minecraft.instance.overlay.addChatMessage("Set Active Editing Camera");
            AC_TileEntityCamera var6 = (AC_TileEntityCamera) var1.getBlockEntity(var2, var3, var4);
            ((ExMinecraft) Minecraft.instance).setActiveCutsceneCamera(var6.camera);
            var6.camera.loadCameraEntities();
            AC_GuiCameraBlock.showUI(var6);
            return true;
        } else {
            return false;
        }
    }

    public boolean isCollidable() {
        return AC_DebugMode.active;
    }
}
