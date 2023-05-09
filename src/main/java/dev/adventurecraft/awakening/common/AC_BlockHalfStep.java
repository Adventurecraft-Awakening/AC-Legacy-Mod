package dev.adventurecraft.awakening.common;

import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class AC_BlockHalfStep extends AC_BlockSolid {

	protected AC_BlockHalfStep(int var1, int var2) {
		super(var1, var2);
	}

    @Override
	public int getTextureForSide(int var1, int var2) {
		if(var2 % 2 == 0) {
			this.setBoundingBox(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F);
		} else {
			this.setBoundingBox(0.0F, 0.5F, 0.0F, 1.0F, 1.0F, 1.0F);
		}

		var2 = 2 * (var2 / 2);
		return var1 <= 1 ? this.texture + var2 + 1 : this.texture + var2;
	}

    @Override
	public boolean isSideRendered(BlockView var1, int var2, int var3, int var4, int var5) {
		this.updateBlockBounds(var1, var2, var3, var4);
		return super.isSideRendered(var1, var2, var3, var4, var5);
	}

    @Override
	public AxixAlignedBoundingBox getCollisionShape(World var1, int var2, int var3, int var4) {
		this.updateBlockBounds(var1, var2, var3, var4);
		return super.getCollisionShape(var1, var2, var3, var4);
	}

	private void updateBlockBounds(BlockView var1, int var2, int var3, int var4) {
		int var5 = var1.getBlockMeta(var2, var3, var4);
		if(var5 % 2 == 0) {
			this.setBoundingBox(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F);
		} else {
			this.setBoundingBox(0.0F, 0.5F, 0.0F, 1.0F, 1.0F, 1.0F);
		}
	}

    @Override
	public boolean isFullOpaque() {
		return false;
	}
}
