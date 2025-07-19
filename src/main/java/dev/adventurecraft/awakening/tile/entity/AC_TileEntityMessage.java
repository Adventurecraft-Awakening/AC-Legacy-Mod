package dev.adventurecraft.awakening.tile.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.tile.entity.TileEntity;

public class AC_TileEntityMessage extends TileEntity {
	public String message = "";
	public String sound = "";

	public void load(CompoundTag var1) {
		super.load(var1);
		this.message = var1.getString("message");
		this.sound = var1.getString("sound");
	}

	public void save(CompoundTag var1) {
		super.save(var1);
		if(this.message != null && !this.message.isEmpty()) {
			var1.putString("message", this.message);
		}

		var1.putString("sound", this.sound);
	}
}
