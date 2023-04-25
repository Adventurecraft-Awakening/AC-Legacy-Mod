package dev.adventurecraft.awakening.common;

import net.minecraft.entity.BlockEntity;
import net.minecraft.util.io.CompoundTag;

public class AC_TileEntityMinMax extends BlockEntity {
	public int minX;
	public int minY;
	public int minZ;
	public int maxX;
	public int maxY;
	public int maxZ;

	public void readNBT(CompoundTag var1) {
		super.readNBT(var1);
		this.minX = var1.getInt("minX");
		this.minY = var1.getInt("minY");
		this.minZ = var1.getInt("minZ");
		this.maxX = var1.getInt("maxX");
		this.maxY = var1.getInt("maxY");
		this.maxZ = var1.getInt("maxZ");
	}

	public void writeNBT(CompoundTag var1) {
		super.writeNBT(var1);
		var1.put("minX", this.minX);
		var1.put("minY", this.minY);
		var1.put("minZ", this.minZ);
		var1.put("maxX", this.maxX);
		var1.put("maxY", this.maxY);
		var1.put("maxZ", this.maxZ);
	}

	public boolean isSet() {
		return this.minX != 0 || this.minY != 0 || this.minZ != 0 || this.maxX != 0 || this.maxY != 0 || this.maxZ != 0;
	}
}
