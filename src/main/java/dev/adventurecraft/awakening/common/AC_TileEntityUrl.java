package dev.adventurecraft.awakening.common;

import net.minecraft.entity.BlockEntity;
import net.minecraft.util.io.CompoundTag;

public class AC_TileEntityUrl extends BlockEntity {
	public String url = "";

	public void readNBT(CompoundTag var1) {
		super.readNBT(var1);
		this.url = var1.getString("url");
	}

	public void writeNBT(CompoundTag var1) {
		super.writeNBT(var1);
		if(this.url != null && !this.url.equals("")) {
			var1.put("url", this.url);
		}

	}
}
