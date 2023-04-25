package dev.adventurecraft.awakening.common;

import net.minecraft.util.io.CompoundTag;

public class AC_TileEntityRedstoneTrigger extends AC_TileEntityMinMax {
	public boolean isActivated = false;
	public boolean visited;
	public boolean resetOnTrigger;

	public void readNBT(CompoundTag var1) {
		super.readNBT(var1);
		this.resetOnTrigger = var1.getBoolean("ResetOnTrigger");
		this.isActivated = var1.getBoolean("IsActivated");
	}

	public void writeNBT(CompoundTag var1) {
		super.writeNBT(var1);
		var1.put("ResetOnTrigger", this.resetOnTrigger);
		var1.put("IsActivated", this.isActivated);
	}
}
