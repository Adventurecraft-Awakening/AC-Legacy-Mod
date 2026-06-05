package dev.adventurecraft.awakening.tile.entity;

import dev.adventurecraft.awakening.tile.AC_Blocks;
import dev.adventurecraft.awakening.world.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

public class AC_TileEntityTriggerMemory extends AC_TileEntityMinMax {

    public boolean isActivated;
    public boolean activateOnDetrigger;
    public boolean resetOnDeath;

    public void set(@NotNull BlockPos min, @NotNull BlockPos max) {
        if (this.isSet() && this.isActivated) {
            AC_Blocks.triggerMemory.triggerDeactivate(this.level, this.x, this.y, this.z);
        }

        this.setMin(min);
        this.setMax(max);

        if (this.isActivated) {
            AC_Blocks.triggerMemory.triggerActivate(this.level, this.x, this.y, this.z);
        }
    }

    public @Override void load(CompoundTag tag) {
        super.load(tag);
        this.isActivated = tag.getBoolean("IsActivated");
        this.activateOnDetrigger = tag.getBoolean("ActivateOnDetrigger");
        this.resetOnDeath = tag.getBoolean("ResetOnDeath");
    }

    public @Override void save(CompoundTag tag) {
        super.save(tag);
        tag.putBoolean("IsActivated", this.isActivated);
        tag.putBoolean("ActivateOnDetrigger", this.activateOnDetrigger);
        tag.putBoolean("ResetOnDeath", this.resetOnDeath);
    }
}
