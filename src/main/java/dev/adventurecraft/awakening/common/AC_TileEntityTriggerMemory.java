package dev.adventurecraft.awakening.common;

import net.minecraft.util.io.CompoundTag;

public class AC_TileEntityTriggerMemory extends AC_TileEntityMinMax {
    public boolean isActivated;
    public boolean activateOnDetrigger;
    public boolean resetOnDeath;

    public void set(int var1, int var2, int var3, int var4, int var5, int var6) {
        if (this.isSet() && this.isActivated) {
            AC_Blocks.triggerMemory.triggerDeactivate(this.world, this.x, this.y, this.z);
        }

        this.minX = var1;
        this.minY = var2;
        this.minZ = var3;
        this.maxX = var4;
        this.maxY = var5;
        this.maxZ = var6;
        if (this.isActivated) {
            AC_Blocks.triggerMemory.triggerActivate(this.world, this.x, this.y, this.z);
        }
    }

    public void readNBT(CompoundTag var1) {
        super.readNBT(var1);
        this.isActivated = var1.getBoolean("IsActivated");
        this.activateOnDetrigger = var1.getBoolean("ActivateOnDetrigger");
        this.resetOnDeath = var1.getBoolean("ResetOnDeath");
    }

    public void writeNBT(CompoundTag var1) {
        super.writeNBT(var1);
        var1.put("IsActivated", this.isActivated);
        var1.put("ActivateOnDetrigger", this.activateOnDetrigger);
        var1.put("ResetOnDeath", this.resetOnDeath);
    }
}
