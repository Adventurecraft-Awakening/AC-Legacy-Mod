package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

public class AC_TileEntityTrigger extends AC_TileEntityMinMax {

    public int activated = 0;
    public boolean visited;
    public boolean resetOnTrigger;

    public void tick() {
        if (this.activated > 0 && !((ExMinecraft) Minecraft.instance).isCameraActive()) {
            --this.activated;
            if (this.activated == 0 && this.level.getTile(this.x, this.y, this.z) == AC_Blocks.triggerBlock.id) {
                AC_Blocks.triggerBlock.deactivateTrigger(this.level, this.x, this.y, this.z);
            }
        }
    }

    public void load(CompoundTag tag) {
        super.load(tag);
        this.resetOnTrigger = tag.getBoolean("ResetOnTrigger");
    }

    public void save(CompoundTag tag) {
        super.save(tag);
        tag.putBoolean("ResetOnTrigger", this.resetOnTrigger);
    }
}
