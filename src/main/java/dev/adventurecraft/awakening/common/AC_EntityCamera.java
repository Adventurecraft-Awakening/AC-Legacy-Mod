package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.common.gui.AC_GuiCamera;
import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class AC_EntityCamera extends LivingEntity {

    private float time;
    private AC_CutsceneCameraBlendType type;
    private int cameraID;

    public AC_EntityCamera(Level world, float time, AC_CutsceneCameraBlendType type, int id) {
        super(world);
        this.time = time;
        this.cameraID = id;
        this.type = type;
    }

    @Override
    protected void defineSynchedData() {
    }

    public void deleteCameraPoint() {
        AC_CutsceneCamera activeCamera = ((ExMinecraft) Minecraft.instance).getActiveCutsceneCamera();
        activeCamera.deletePoint(this.getCameraId());
        activeCamera.loadCameraEntities();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag var1) {
    }

    @Override
    public void readAdditionalSaveData(CompoundTag var1) {
    }

    @Override
    public void baseTick() {
    }

    @Override
    public void aiStep() {
    }

    @Override
    public void tick() {
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean interact(Player var1) {
        AC_GuiCamera.showUI(this);
        return true;
    }

    public float getTime() {
        return time;
    }

    public void setTime(float time) {
        this.time = time;
    }

    public AC_CutsceneCameraBlendType getBlendType() {
        return type;
    }

    public void setBlendType(AC_CutsceneCameraBlendType type) {
        this.type = type;
    }

    public int getCameraId() {
        return cameraID;
    }

    public void setCameraId(int cameraID) {
        this.cameraID = cameraID;
    }
}
