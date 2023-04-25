package dev.adventurecraft.awakening.common;

import net.minecraft.util.io.CompoundTag;

public class AC_TileEntityTriggerPushable extends AC_TileEntityMinMax {
	public boolean activated;
	public boolean resetOnTrigger;

	public void readNBT(CompoundTag var1) {
		super.readNBT(var1);
		this.resetOnTrigger = var1.getBoolean("ResetOnTrigger");
		this.activated = var1.getBoolean("activated");
	}

	public void writeNBT(CompoundTag var1) {
		super.writeNBT(var1);
		var1.put("ResetOnTrigger", this.resetOnTrigger);
		var1.put("activated", this.activated);
	}
}
