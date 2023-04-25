package dev.adventurecraft.awakening.common;

import java.util.Iterator;

import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.BlockEntity;
import net.minecraft.util.io.CompoundTag;

public class AC_TileEntityCamera extends BlockEntity {
    public String message;
    public String sound;
    public AC_CutsceneCamera camera = new AC_CutsceneCamera();
    int type = 2;
    public boolean pauseGame = true;

    public void loadCamera() {
        this.copyCamera(this.camera, ((ExMinecraft) Minecraft.instance).getCutsceneCamera());
        ((ExMinecraft) Minecraft.instance).getCutsceneCamera().startType = this.type;
    }

    public void saveCamera() {
        this.copyCamera(((ExMinecraft) Minecraft.instance).getCutsceneCamera(), this.camera);
    }

    private void copyCamera(AC_CutsceneCamera var1, AC_CutsceneCamera var2) {
        var2.clearPoints();

        for (AC_CutsceneCameraPoint var4 : var1.cameraPoints) {
            var2.addCameraPoint(var4.time, var4.posX, var4.posY, var4.posZ, var4.rotYaw, var4.rotPitch, var4.cameraBlendType);
        }
    }

    public void readNBT(CompoundTag var1) {
        super.readNBT(var1);
        int var2 = var1.getInt("numPoints");

        for (int var3 = 0; var3 < var2; ++var3) {
            this.readPointTag(var1.getCompoundTag(String.format("point%d", var3)));
        }

        if (var1.containsKey("type")) {
            this.type = var1.getByte("type");
        }

        if (var1.containsKey("pauseGame")) {
            this.pauseGame = var1.getBoolean("pauseGame");
        }

    }

    public void writeNBT(CompoundTag var1) {
        super.writeNBT(var1);
        int var2 = 0;

        for (AC_CutsceneCameraPoint var4 : this.camera.cameraPoints) {
            var1.put(String.format("point%d", var2), this.getPointTag(var4));
            ++var2;
        }

        var1.put("numPoints", var2);
        var1.put("type", (byte) this.type);
        var1.put("pauseGame", this.pauseGame);
    }

    private CompoundTag getPointTag(AC_CutsceneCameraPoint var1) {
        CompoundTag var2 = new CompoundTag();
        var2.put("time", var1.time);
        var2.put("posX", var1.posX);
        var2.put("posY", var1.posY);
        var2.put("posZ", var1.posZ);
        var2.put("yaw", var1.rotYaw);
        var2.put("pitch", var1.rotPitch);
        var2.put("type", (byte) var1.cameraBlendType);
        return var2;
    }

    private void readPointTag(CompoundTag var1) {
        float var5 = var1.getFloat("time");
        float var2 = var1.getFloat("posX");
        float var3 = var1.getFloat("posY");
        float var4 = var1.getFloat("posZ");
        float var6 = var1.getFloat("yaw");
        float var7 = var1.getFloat("pitch");
        byte var8 = 2;
        if (var1.containsKey("type")) {
            var8 = var1.getByte("type");
        }

        this.camera.addCameraPoint(var5, var2, var3, var4, var6, var7, var8);
    }
}
