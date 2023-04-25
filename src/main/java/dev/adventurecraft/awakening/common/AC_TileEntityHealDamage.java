package dev.adventurecraft.awakening.common;

import net.minecraft.entity.BlockEntity;
import net.minecraft.util.io.CompoundTag;

public class AC_TileEntityHealDamage extends BlockEntity {
	public int healDamage;

	public void readNBT(CompoundTag var1) {
		super.readNBT(var1);
		this.healDamage = var1.getInt("healDamage");
	}

	public void writeNBT(CompoundTag var1) {
		super.writeNBT(var1);
		var1.put("healDamage", this.healDamage);
	}
}
