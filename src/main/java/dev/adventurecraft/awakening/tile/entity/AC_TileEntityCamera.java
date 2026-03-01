package dev.adventurecraft.awakening.tile.entity;

import dev.adventurecraft.awakening.common.AC_CutsceneCamera;
import dev.adventurecraft.awakening.common.AC_CutsceneCameraBlendType;
import dev.adventurecraft.awakening.common.AC_CutsceneCameraPoint;
import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import dev.adventurecraft.awakening.extension.util.io.ExCompoundTag;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.tile.entity.TileEntity;

public class AC_TileEntityCamera extends TileEntity {

    public String message;
    public String sound;
    private AC_CutsceneCamera camera;
    private AC_CutsceneCameraBlendType type = AC_CutsceneCameraBlendType.QUADRATIC;
    public boolean pauseGame = true;

    public void loadCamera() {
        this.copyCamera(this.getCamera(), ((ExMinecraft) Minecraft.instance).getCutsceneCamera());
        ((ExMinecraft) Minecraft.instance).getCutsceneCamera().startType = this.getBlendType();
    }

    public void saveCamera() {
        this.copyCamera(((ExMinecraft) Minecraft.instance).getCutsceneCamera(), this.getCamera());
    }

    private void copyCamera(AC_CutsceneCamera src, AC_CutsceneCamera dst) {
        dst.clearPoints();

        for (AC_CutsceneCameraPoint p : src.cameraPoints) {
            dst.addCameraPoint(p.time, p.posX, p.posY, p.posZ, p.rotYaw, p.rotPitch, p.blendType);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        var exTag = (ExCompoundTag) tag;

        int pointCount = tag.getInt("numPoints");
        for (int i = 0; i < pointCount; ++i) {
            this.readPointTag(tag.getCompoundTag(String.format("point%d", i)));
        }

        exTag.findByte("type").map(AC_CutsceneCameraBlendType::get).ifPresent(this::setBlendType);
        exTag.findBool("pauseGame").ifPresent(b -> this.pauseGame = b);
    }

    @Override
    public void save(CompoundTag tag) {
        super.save(tag);

        int pointCount = 0;
        for (AC_CutsceneCameraPoint point : this.getCamera().cameraPoints) {
            tag.putCompoundTag(String.format("point%d", pointCount), this.getPointTag(point));
            ++pointCount;
        }

        tag.putInt("numPoints", pointCount);
        tag.putByte("type", (byte) this.getBlendType().value);
        tag.putBoolean("pauseGame", this.pauseGame);
    }

    private CompoundTag getPointTag(AC_CutsceneCameraPoint point) {
        var tag = new CompoundTag();
        tag.putFloat("time", point.time);
        tag.putFloat("posX", point.posX);
        tag.putFloat("posY", point.posY);
        tag.putFloat("posZ", point.posZ);
        tag.putFloat("yaw", point.rotYaw);
        tag.putFloat("pitch", point.rotPitch);
        tag.putByte("type", (byte) point.blendType.value);
        return tag;
    }

    private void readPointTag(CompoundTag tag) {
        float time = tag.getFloat("time");
        float x = tag.getFloat("posX");
        float y = tag.getFloat("posY");
        float z = tag.getFloat("posZ");
        float yaw = tag.getFloat("yaw");
        float pitch = tag.getFloat("pitch");

        var type = ((ExCompoundTag) tag)
            .findByte("type")
            .map(AC_CutsceneCameraBlendType::get)
            .orElse(AC_CutsceneCameraBlendType.QUADRATIC);

        this.getCamera().addCameraPoint(time, x, y, z, yaw, pitch, type);
    }

    public AC_CutsceneCameraBlendType getBlendType() {
        return this.type;
    }

    public void setBlendType(AC_CutsceneCameraBlendType type) {
        this.type = type;
    }

    public AC_CutsceneCamera getCamera() {
        if (this.camera == null) {
            // FIXME: this.level is null on load...
            this.camera = new AC_CutsceneCamera(Minecraft.instance.level);
        }
        return this.camera;
    }
}
