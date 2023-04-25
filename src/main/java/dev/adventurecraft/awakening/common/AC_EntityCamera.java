package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.io.CompoundTag;
import net.minecraft.world.World;

public class AC_EntityCamera extends LivingEntity {
    float time;
    int type;
    int cameraID;

    public AC_EntityCamera(World var1, float var2, int var3, int var4) {
        super(var1);
        this.time = var2;
        this.cameraID = var4;
        this.type = var3;
    }

    protected void initDataTracker() {
    }

    public void deleteCameraPoint() {
        AC_CutsceneCamera activeCamera = ((ExMinecraft) Minecraft.instance).getActiveCutsceneCamera();
        activeCamera.deletePoint(this.cameraID);
        activeCamera.loadCameraEntities();
    }

    public void readAdditional(CompoundTag var1) {
    }

    public void writeAdditional(CompoundTag var1) {
    }

    public void baseTick() {
    }

    public void updateDespawnCounter() {
    }

    public void tick() {
    }

    public boolean method_1356() {
        return true;
    }

    public boolean method_1380() {
        return false;
    }

    public boolean interact(PlayerEntity var1) {
        AC_GuiCamera.showUI(this);
        return true;
    }
}
