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

    public void readNBT(CompoundTag var1) {
        super.readNBT(var1);
        this.resetOnTrigger = var1.getBoolean("ResetOnTrigger");
    }

    public void writeNBT(CompoundTag var1) {
        super.writeNBT(var1);
        var1.put("ResetOnTrigger", this.resetOnTrigger);
    }
}
