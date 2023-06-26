package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.util.io.CompoundTag;

public class AC_TileEntityTrigger extends AC_TileEntityMinMax {

    public int activated = 0;
    public boolean visited;
    public boolean resetOnTrigger;

    public void tick() {
        if (this.activated > 0 && !((ExMinecraft) Minecraft.instance).isCameraActive()) {
            --this.activated;
            if (this.activated == 0 && this.world.getBlockId(this.x, this.y, this.z) == AC_Blocks.triggerBlock.id) {
                AC_Blocks.triggerBlock.deactivateTrigger(this.world, this.x, this.y, this.z);
            }
        }
    }

    public void readNBT(CompoundTag tag) {
        super.readNBT(tag);
        this.resetOnTrigger = tag.getBoolean("ResetOnTrigger");
    }

    public void writeNBT(CompoundTag tag) {
        super.writeNBT(tag);
        tag.put("ResetOnTrigger", this.resetOnTrigger);
    }
}
