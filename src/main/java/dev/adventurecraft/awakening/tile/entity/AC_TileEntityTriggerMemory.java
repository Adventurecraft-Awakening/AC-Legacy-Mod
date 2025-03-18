package dev.adventurecraft.awakening.tile.entity;

import dev.adventurecraft.awakening.tile.AC_Blocks;
import net.minecraft.nbt.CompoundTag;

public class AC_TileEntityTriggerMemory extends AC_TileEntityMinMax {

    public boolean isActivated;
    public boolean activateOnDetrigger;
    public boolean resetOnDeath;

    public void set(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        if (this.isSet() && this.isActivated) {
            AC_Blocks.triggerMemory.triggerDeactivate(this.level, this.x, this.y, this.z);
        }

        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
        if (this.isActivated) {
            AC_Blocks.triggerMemory.triggerActivate(this.level, this.x, this.y, this.z);
        }
    }

    public void load(CompoundTag tag) {
        super.load(tag);
        this.isActivated = tag.getBoolean("IsActivated");
        this.activateOnDetrigger = tag.getBoolean("ActivateOnDetrigger");
        this.resetOnDeath = tag.getBoolean("ResetOnDeath");
    }

    public void save(CompoundTag tag) {
        super.save(tag);
        tag.putBoolean("IsActivated", this.isActivated);
        tag.putBoolean("ActivateOnDetrigger", this.activateOnDetrigger);
        tag.putBoolean("ResetOnDeath", this.resetOnDeath);
    }
}
