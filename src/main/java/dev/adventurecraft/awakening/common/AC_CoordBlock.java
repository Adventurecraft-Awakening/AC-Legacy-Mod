package dev.adventurecraft.awakening.common;

import java.util.ArrayList;
import java.util.List;

public final class AC_CoordBlock {
	private static final List<AC_CoordBlock> blockCoords = new ArrayList<>();
	public static int numBlockCoordsInUse = 0;

	public int x;
	public int y;
	public int z;

	public AC_CoordBlock(int var1, int var2, int var3) {
		this.x = var1;
		this.y = var2;
		this.z = var3;
	}

	public static AC_CoordBlock getFromPool(int var0, int var1, int var2) {
		if(numBlockCoordsInUse >= blockCoords.size()) {
			blockCoords.add(new AC_CoordBlock(var0, var1, var2));
		}

		return blockCoords.get(numBlockCoordsInUse++).set(var0, var1, var2);
	}

	public static void resetPool() {
		numBlockCoordsInUse = 0;
	}

	public static void releaseLastOne() {
		--numBlockCoordsInUse;
	}

	public AC_CoordBlock set(int var1, int var2, int var3) {
		this.x = var1;
		this.y = var2;
		this.z = var3;
		return this;
	}

	public boolean isEqual(int var1, int var2, int var3) {
		return this.x == var1 && this.y == var2 && this.z == var3;
	}

	public boolean equals(Object var1) {
		if(!(var1 instanceof AC_CoordBlock)) {
			return false;
		} else {
			AC_CoordBlock var2 = (AC_CoordBlock)var1;
			return this.x == var2.x && this.y == var2.y && this.z == var2.z;
		}
	}

	public int hashCode() {
		return this.x << 16 ^ this.z ^ this.y << 24;
	}
}
