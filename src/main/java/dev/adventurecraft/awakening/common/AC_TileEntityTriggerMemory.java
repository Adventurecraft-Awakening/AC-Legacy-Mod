package dev.adventurecraft.awakening.common;

import net.minecraft.util.io.CompoundTag;

public class AC_TileEntityTriggerMemory extends AC_TileEntityMinMax {

    public boolean isActivated;
    public boolean activateOnDetrigger;
    public boolean resetOnDeath;

    public void set(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        if (this.isSet() && this.isActivated) {
            AC_Blocks.triggerMemory.triggerDeactivate(this.world, this.x, this.y, this.z);
        }

        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
        if (this.isActivated) {
            AC_Blocks.triggerMemory.triggerActivate(this.world, this.x, this.y, this.z);
        }
    }

    public void readNBT(CompoundTag tag) {
        super.readNBT(tag);
        this.isActivated = tag.getBoolean("IsActivated");
        this.activateOnDetrigger = tag.getBoolean("ActivateOnDetrigger");
        this.resetOnDeath = tag.getBoolean("ResetOnDeath");
    }

    public void writeNBT(CompoundTag tag) {
        super.writeNBT(tag);
        tag.put("IsActivated", this.isActivated);
        tag.put("ActivateOnDetrigger", this.activateOnDetrigger);
        tag.put("ResetOnDeath", this.resetOnDeath);
    }
}
