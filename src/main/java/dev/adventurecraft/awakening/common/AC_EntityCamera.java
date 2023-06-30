package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.io.CompoundTag;
import net.minecraft.world.World;

public class AC_EntityCamera extends LivingEntity {

    float time;
    AC_CutsceneCameraBlendType type;
    int cameraID;

    public AC_EntityCamera(World world, float time, AC_CutsceneCameraBlendType type, int id) {
        super(world);
        this.time = time;
        this.cameraID = id;
        this.type = type;
    }

    @Override
    protected void initDataTracker() {
    }

    public void deleteCameraPoint() {
        AC_CutsceneCamera activeCamera = ((ExMinecraft) Minecraft.instance).getActiveCutsceneCamera();
        activeCamera.deletePoint(this.cameraID);
        activeCamera.loadCameraEntities();
    }

    @Override
    public void readAdditional(CompoundTag var1) {
    }

    @Override
    public void writeAdditional(CompoundTag var1) {
    }

    @Override
    public void baseTick() {
    }

    @Override
    public void updateDespawnCounter() {
    }

    @Override
    public void tick() {
    }

    @Override
    public boolean method_1356() {
        return true;
    }

    @Override
    public boolean method_1380() {
        return false;
    }

    @Override
    public boolean interact(PlayerEntity var1) {
        AC_GuiCamera.showUI(this);
        return true;
    }
}
