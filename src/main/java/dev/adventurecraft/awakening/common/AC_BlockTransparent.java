package dev.adventurecraft.awakening.common;

import net.minecraft.world.BlockView;

public class AC_BlockTransparent extends AC_BlockSolid {
	protected AC_BlockTransparent(int var1, int var2) {
		super(var1, var2);
	}

	public boolean isFullOpaque() {
		return false;
	}

	public boolean isSideRendered(BlockView var1, int var2, int var3, int var4, int var5) {
		int var6 = var1.getBlockId(var2, var3, var4);
		return var6 == this.id ? false : super.isSideRendered(var1, var2, var3, var4, var5);
	}
}
