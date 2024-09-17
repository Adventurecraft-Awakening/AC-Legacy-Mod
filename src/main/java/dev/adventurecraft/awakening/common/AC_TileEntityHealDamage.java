package dev.adventurecraft.awakening.common;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.tile.entity.TileEntity;

public class AC_TileEntityHealDamage extends TileEntity {
	public int healDamage;

	public void load(CompoundTag var1) {
		super.load(var1);
		this.healDamage = var1.getInt("healDamage");
	}

	public void save(CompoundTag var1) {
		super.save(var1);
		var1.putInt("healDamage", this.healDamage);
	}
}
