package dev.adventurecraft.awakening.common;

import net.minecraft.nbt.CompoundTag;

public class AC_TileEntityRedstoneTrigger extends AC_TileEntityMinMax {

    public boolean isActivated = false;
	public boolean resetOnTrigger;

	public void load(CompoundTag tag) {
		super.load(tag);
		this.resetOnTrigger = tag.getBoolean("ResetOnTrigger");
		this.isActivated = tag.getBoolean("IsActivated");
	}

	public void save(CompoundTag tag) {
		super.save(tag);
		tag.putBoolean("ResetOnTrigger", this.resetOnTrigger);
		tag.putBoolean("IsActivated", this.isActivated);
	}
}
