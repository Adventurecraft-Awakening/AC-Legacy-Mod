package dev.adventurecraft.awakening.common;

import net.minecraft.entity.BlockEntity;
import net.minecraft.util.io.CompoundTag;

public class AC_TileEntityTeleport extends BlockEntity {
	public int x;
	public int y;
	public int z;

	public void readNBT(CompoundTag var1) {
		super.readNBT(var1);
		this.x = var1.getInt("teleportX");
		this.y = var1.getInt("teleportY");
		this.z = var1.getInt("teleportZ");
	}

	public void writeNBT(CompoundTag var1) {
		super.writeNBT(var1);
		var1.put("teleportX", this.x);
		var1.put("teleportY", this.y);
		var1.put("teleportZ", this.z);
	}
}
