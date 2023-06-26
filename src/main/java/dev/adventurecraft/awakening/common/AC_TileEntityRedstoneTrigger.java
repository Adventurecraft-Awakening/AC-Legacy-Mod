package dev.adventurecraft.awakening.common;

import net.minecraft.util.io.CompoundTag;

public class AC_TileEntityRedstoneTrigger extends AC_TileEntityMinMax {

    public boolean isActivated = false;
	public boolean resetOnTrigger;

	public void readNBT(CompoundTag tag) {
		super.readNBT(tag);
		this.resetOnTrigger = tag.getBoolean("ResetOnTrigger");
		this.isActivated = tag.getBoolean("IsActivated");
	}

	public void writeNBT(CompoundTag tag) {
		super.writeNBT(tag);
		tag.put("ResetOnTrigger", this.resetOnTrigger);
		tag.put("IsActivated", this.isActivated);
	}
}
