package dev.adventurecraft.awakening.common;

import net.minecraft.util.io.CompoundTag;

public class AC_TileEntityTriggerPushable extends AC_TileEntityMinMax {

    public boolean activated;
	public boolean resetOnTrigger;

	public void readNBT(CompoundTag tag) {
		super.readNBT(tag);
		this.resetOnTrigger = tag.getBoolean("ResetOnTrigger");
		this.activated = tag.getBoolean("activated");
	}

	public void writeNBT(CompoundTag tag) {
		super.writeNBT(tag);
		tag.put("ResetOnTrigger", this.resetOnTrigger);
		tag.put("activated", this.activated);
	}
}
