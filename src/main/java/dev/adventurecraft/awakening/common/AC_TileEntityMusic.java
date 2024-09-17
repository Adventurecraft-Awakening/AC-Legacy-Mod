package dev.adventurecraft.awakening.common;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.tile.entity.TileEntity;

public class AC_TileEntityMusic extends TileEntity {
	public String musicName = "";
	public int fadeOut = 500;
	public int fadeIn = 500;

	public void load(CompoundTag var1) {
		super.load(var1);
		this.musicName = var1.getString("musicName");
		this.fadeOut = var1.getInt("fadeOut");
		this.fadeIn = var1.getInt("fadeIn");
	}

	public void save(CompoundTag var1) {
		super.save(var1);
		if(this.musicName != null && !this.musicName.equals("")) {
			var1.putString("musicName", this.musicName);
		}

		var1.putInt("fadeOut", this.fadeOut);
		var1.putInt("fadeIn", this.fadeIn);
	}
}
