package dev.adventurecraft.awakening.common;

import net.minecraft.entity.BlockEntity;
import net.minecraft.util.io.CompoundTag;

public class AC_TileEntityMessage extends BlockEntity {
	public String message = "";
	public String sound = "";

	public void readNBT(CompoundTag var1) {
		super.readNBT(var1);
		this.message = var1.getString("message");
		this.sound = var1.getString("sound");
	}

	public void writeNBT(CompoundTag var1) {
		super.writeNBT(var1);
		if(this.message != null && !this.message.equals("")) {
			var1.put("message", this.message);
		}

		var1.put("sound", this.sound);
	}
}
