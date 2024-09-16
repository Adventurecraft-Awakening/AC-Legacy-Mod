package dev.adventurecraft.awakening.common;

public class AC_TileEntityTriggerInverter extends AC_TileEntityMinMax {
	public void set(int var1, int var2, int var3, int var4, int var5, int var6) {
		int var7 = this.level.getData(this.x, this.y, this.z);
		if(this.isSet() && var7 <= 0) {
			AC_Blocks.triggerInverter.onTriggerActivated(this.level, this.x, this.y, this.z);
		}

		this.minX = var1;
		this.minY = var2;
		this.minZ = var3;
		this.maxX = var4;
		this.maxY = var5;
		this.maxZ = var6;
		if(var7 <= 0) {
			AC_Blocks.triggerInverter.onTriggerDeactivated(this.level, this.x, this.y, this.z);
		}

	}
}
