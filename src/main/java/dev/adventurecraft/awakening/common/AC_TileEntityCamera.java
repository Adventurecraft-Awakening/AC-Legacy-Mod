package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.BlockEntity;
import net.minecraft.util.io.CompoundTag;

public class AC_TileEntityCamera extends BlockEntity {

    public String message;
    public String sound;
    public AC_CutsceneCamera camera = new AC_CutsceneCamera();
    AC_CutsceneCameraBlendType type = AC_CutsceneCameraBlendType.QUADRATIC;
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
            var2.addCameraPoint(var4.time, var4.posX, var4.posY, var4.posZ, var4.rotYaw, var4.rotPitch, var4.blendType);
        }
    }

    @Override
    public void readNBT(CompoundTag tag) {
        super.readNBT(tag);
        int pointCount = tag.getInt("numPoints");

        for (int i = 0; i < pointCount; ++i) {
            this.readPointTag(tag.getCompoundTag(String.format("point%d", i)));
        }

        if (tag.containsKey("type")) {
            this.type = AC_CutsceneCameraBlendType.get(tag.getByte("type"));
        }

        if (tag.containsKey("pauseGame")) {
            this.pauseGame = tag.getBoolean("pauseGame");
        }
    }

    @Override
    public void writeNBT(CompoundTag tag) {
        super.writeNBT(tag);

        int pointCount = 0;
        for (AC_CutsceneCameraPoint point : this.camera.cameraPoints) {
            tag.put(String.format("point%d", pointCount), this.getPointTag(point));
            ++pointCount;
        }

        tag.put("numPoints", pointCount);
        tag.put("type", (byte) this.type.value);
        tag.put("pauseGame", this.pauseGame);
    }

    private CompoundTag getPointTag(AC_CutsceneCameraPoint point) {
        var tag = new CompoundTag();
        tag.put("time", point.time);
        tag.put("posX", point.posX);
        tag.put("posY", point.posY);
        tag.put("posZ", point.posZ);
        tag.put("yaw", point.rotYaw);
        tag.put("pitch", point.rotPitch);
        tag.put("type", (byte) point.blendType.value);
        return tag;
    }

    private void readPointTag(CompoundTag tag) {
        float time = tag.getFloat("time");
        float x = tag.getFloat("posX");
        float y = tag.getFloat("posY");
        float z = tag.getFloat("posZ");
        float yaw = tag.getFloat("yaw");
        float pitch = tag.getFloat("pitch");
        AC_CutsceneCameraBlendType type = AC_CutsceneCameraBlendType.QUADRATIC;
        if (tag.containsKey("type")) {
            type = AC_CutsceneCameraBlendType.get(tag.getByte("type"));
        }

        this.camera.addCameraPoint(time, x, y, z, yaw, pitch, type);
    }
}
