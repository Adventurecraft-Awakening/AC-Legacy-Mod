package dev.adventurecraft.awakening.common;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.tile.entity.TileEntity;

public class AC_TileEntityUrl extends TileEntity {
	public String url = "";

	public void load(CompoundTag var1) {
		super.load(var1);
		this.url = var1.getString("url");
	}

	public void save(CompoundTag var1) {
		super.save(var1);
		if(this.url != null && !this.url.equals("")) {
			var1.putString("url", this.url);
		}

	}
}
