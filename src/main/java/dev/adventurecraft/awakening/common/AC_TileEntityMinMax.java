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

	public void readNBT(CompoundTag tag) {
		super.readNBT(tag);
		this.minX = tag.getInt("minX");
		this.minY = tag.getInt("minY");
		this.minZ = tag.getInt("minZ");
		this.maxX = tag.getInt("maxX");
		this.maxY = tag.getInt("maxY");
		this.maxZ = tag.getInt("maxZ");
	}

	public void writeNBT(CompoundTag tag) {
		super.writeNBT(tag);
		tag.put("minX", this.minX);
		tag.put("minY", this.minY);
		tag.put("minZ", this.minZ);
		tag.put("maxX", this.maxX);
		tag.put("maxY", this.maxY);
		tag.put("maxZ", this.maxZ);
	}

	public boolean isSet() {
		return this.minX != 0 || this.minY != 0 || this.minZ != 0 || this.maxX != 0 || this.maxY != 0 || this.maxZ != 0;
	}
}
