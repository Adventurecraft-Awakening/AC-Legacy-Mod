package dev.adventurecraft.awakening.common;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.tile.entity.TileEntity;

public class AC_TileEntityMinMax extends TileEntity {

	public int minX;
	public int minY;
	public int minZ;
	public int maxX;
	public int maxY;
	public int maxZ;

	public void load(CompoundTag tag) {
		super.load(tag);
		this.minX = tag.getInt("minX");
		this.minY = tag.getInt("minY");
		this.minZ = tag.getInt("minZ");
		this.maxX = tag.getInt("maxX");
		this.maxY = tag.getInt("maxY");
		this.maxZ = tag.getInt("maxZ");
	}

	public void save(CompoundTag tag) {
		super.save(tag);
		tag.putInt("minX", this.minX);
		tag.putInt("minY", this.minY);
		tag.putInt("minZ", this.minZ);
		tag.putInt("maxX", this.maxX);
		tag.putInt("maxY", this.maxY);
		tag.putInt("maxZ", this.maxZ);
	}

	public boolean isSet() {
		return this.minX != 0 || this.minY != 0 || this.minZ != 0 || this.maxX != 0 || this.maxY != 0 || this.maxZ != 0;
	}
}
