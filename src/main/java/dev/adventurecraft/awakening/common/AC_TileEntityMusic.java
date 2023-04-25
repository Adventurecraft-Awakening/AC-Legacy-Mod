package dev.adventurecraft.awakening.common;

import net.minecraft.entity.BlockEntity;
import net.minecraft.util.io.CompoundTag;

public class AC_TileEntityMusic extends BlockEntity {
	public String musicName = "";
	public int fadeOut = 500;
	public int fadeIn = 500;

	public void readNBT(CompoundTag var1) {
		super.readNBT(var1);
		this.musicName = var1.getString("musicName");
		this.fadeOut = var1.getInt("fadeOut");
		this.fadeIn = var1.getInt("fadeIn");
	}

	public void writeNBT(CompoundTag var1) {
		super.writeNBT(var1);
		if(this.musicName != null && !this.musicName.equals("")) {
			var1.put("musicName", this.musicName);
		}

		var1.put("fadeOut", this.fadeOut);
		var1.put("fadeIn", this.fadeIn);
	}
}
