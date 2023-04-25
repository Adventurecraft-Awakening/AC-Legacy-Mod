package dev.adventurecraft.awakening.common;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.world.World;

public class AC_BlockSpike extends Block {
	protected AC_BlockSpike(int var1) {
		super(var1, 246, Material.METAL);
	}

	public AxixAlignedBoundingBox getCollisionShape(World var1, int var2, int var3, int var4) {
		float var5 = 0.25F;
		return AxixAlignedBoundingBox.createAndAddToList((double)((float)var2 + var5), (double)var3, (double)((float)var4 + var5), (double)((float)(var2 + 1) - var5), (double)((float)(var3 + 1) - var5), (double)((float)(var4 + 1) - var5));
	}

	public boolean isFullCube() {
		return false;
	}

	public boolean isFullOpaque() {
		return false;
	}

	public int getRenderType() {
		return 32;
	}

	public void onEntityCollision(World var1, int var2, int var3, int var4, Entity var5) {
		var5.damage((Entity)null, 10);
	}
}
