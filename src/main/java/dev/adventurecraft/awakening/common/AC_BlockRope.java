package dev.adventurecraft.awakening.common;

import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.world.World;

public class AC_BlockRope extends AC_BlockPlant {
	protected AC_BlockRope(int var1, int var2) {
		super(var1, var2);
		float var3 = 0.2F;
		this.setBoundingBox(0.5F - var3, 0.0F, 0.5F - var3, 0.5F + var3, 1.0F, 0.5F + var3);
	}

	public AxixAlignedBoundingBox getOutlineShape(World var1, int var2, int var3, int var4) {
		this.updateBounds(var1, var2, var3, var4);
		return super.getOutlineShape(var1, var2, var3, var4);
	}

	public AxixAlignedBoundingBox getCollisionShape(World var1, int var2, int var3, int var4) {
		int var5 = var1.getBlockMeta(var2, var3, var4) % 3;
		if(var5 == 0) {
			return null;
		} else {
			this.updateBounds(var1, var2, var3, var4);
			return AxixAlignedBoundingBox.createAndAddToList((double)var2 + this.minX, (double)var3 + this.minY, (double)var4 + this.minZ, (double)var2 + this.maxX, (double)var3 + this.maxY, (double)var4 + this.maxZ);
		}
	}

	private void updateBounds(World var1, int var2, int var3, int var4) {
		int var5 = var1.getBlockMeta(var2, var3, var4) % 3;
		float var6 = 0.2F;
		if(var5 == 0) {
			this.setBoundingBox(0.5F - var6, 0.0F, 0.5F - var6, 0.5F + var6, 1.0F, 0.5F + var6);
		} else if(var5 == 1) {
			this.setBoundingBox(0.0F, 0.5F - var6, 0.5F - var6, 1.0F, 0.5F + var6, 0.5F + var6);
		} else {
			this.setBoundingBox(0.5F - var6, 0.5F - var6, 0.0F, 0.5F + var6, 0.5F + var6, 1.0F);
		}

	}

	public int getRenderType() {
		return 35;
	}

	public int getTextureForSide(int var1, int var2) {
		return this.texture + var2 / 3;
	}
}
