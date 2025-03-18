package dev.adventurecraft.awakening.tile.entity;

import net.minecraft.nbt.CompoundTag;

public class AC_TileEntityTriggerPushable extends AC_TileEntityMinMax {

    public boolean activated;
	public boolean resetOnTrigger;

	public void load(CompoundTag tag) {
		super.load(tag);
		this.resetOnTrigger = tag.getBoolean("ResetOnTrigger");
		this.activated = tag.getBoolean("activated");
	}

	public void save(CompoundTag tag) {
		super.save(tag);
		tag.putBoolean("ResetOnTrigger", this.resetOnTrigger);
		tag.putBoolean("activated", this.activated);
	}
}
