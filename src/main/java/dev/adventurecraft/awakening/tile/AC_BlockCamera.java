package dev.adventurecraft.awakening.tile;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.common.gui.AC_GuiCameraBlock;
import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import dev.adventurecraft.awakening.tile.entity.AC_TileEntityCamera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.TileEntityTile;
import net.minecraft.world.level.tile.entity.TileEntity;
import net.minecraft.world.phys.AABB;

public class AC_BlockCamera extends TileEntityTile implements AC_ITriggerDebugBlock {

    protected AC_BlockCamera(int var1, int var2) {
        super(var1, var2, Material.AIR);
    }

    @Override
    protected TileEntity newTileEntity() {
        return new AC_TileEntityCamera();
    }

    @Override
    public boolean isSolidRender() {
        return false;
    }

    @Override
    public AABB getAABB(Level var1, int var2, int var3, int var4) {
        return null;
    }

    @Override
    public void onTriggerActivated(Level world, int x, int y, int z) {
        if (!(world.getTileEntity(x, y, z) instanceof AC_TileEntityCamera entityCamera)) {
            return;
        }
        entityCamera.loadCamera();
        ExMinecraft mc = (ExMinecraft) Minecraft.instance;
        mc.getCutsceneCamera().startCamera();
        mc.setCameraActive(true);
        mc.setCameraPause(entityCamera.pauseGame);
    }

    @Override
    public void onTriggerDeactivated(Level world, int x, int y, int z) {
    }

    @Override
    public boolean use(Level world, int x, int y, int z, Player player) {
        if (!AC_DebugMode.active) {
            return false;
        }
        if (!(world.getTileEntity(x, y, z) instanceof AC_TileEntityCamera entityCamera)) {
            return false;
        }
        Minecraft.instance.gui.addMessage("Set Active Editing Camera");
        ((ExMinecraft) Minecraft.instance).setActiveCutsceneCamera(entityCamera.getCamera());
        entityCamera.getCamera().loadCameraEntities();
        AC_GuiCameraBlock.showUI(entityCamera);
        return true;
    }

    @Override
    public boolean mayPick() {
        return AC_DebugMode.active;
    }
}
