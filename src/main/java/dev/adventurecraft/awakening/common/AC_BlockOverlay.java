package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.block.ExBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class AC_BlockOverlay extends Block implements AC_IBlockColor {
	protected AC_BlockOverlay(int var1, int var2) {
		super(var1, var2, Material.PLANT);
		this.setBoundingBox(0.0F, 0.0F, 0.0F, 1.0F, 0.1F, 1.0F);
	}

	public int getTextureForSide(int var1, int var2) {
		return this.texture + var2;
	}

	public AxixAlignedBoundingBox getCollisionShape(World var1, int var2, int var3, int var4) {
		this.updateBounds(var1, var2, var3, var4);
		return null;
	}

	public AxixAlignedBoundingBox getOutlineShape(World var1, int var2, int var3, int var4) {
		this.updateBounds(var1, var2, var3, var4);
		return super.getOutlineShape(var1, var2, var3, var4);
	}

	public void updateBounds(BlockView var1, int var2, int var3, int var4) {
		if(var1.method_1783(var2, var3 - 1, var4)) {
			this.setBoundingBox(0.0F, 0.0F, 0.0F, 1.0F, 0.01F, 1.0F);
		} else if(var1.method_1783(var2, var3 + 1, var4)) {
			this.setBoundingBox(0.0F, 0.99F, 0.0F, 1.0F, 1.0F, 1.0F);
		} else if(var1.method_1783(var2 - 1, var3, var4)) {
			this.setBoundingBox(0.0F, 0.0F, 0.0F, 0.01F, 1.0F, 1.0F);
		} else if(var1.method_1783(var2 + 1, var3, var4)) {
			this.setBoundingBox(0.99F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
		} else if(var1.method_1783(var2, var3, var4 - 1)) {
			this.setBoundingBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.01F);
		} else if(var1.method_1783(var2, var3, var4 + 1)) {
			this.setBoundingBox(0.0F, 0.0F, 0.99F, 1.0F, 1.0F, 1.0F);
		} else {
			this.setBoundingBox(0.0F, 0.0F, 0.0F, 1.0F, 0.01F, 1.0F);
		}

	}

	public boolean isFullOpaque() {
		return false;
	}

	public boolean isCollidable() {
		return AC_DebugMode.active;
	}

	public boolean isFullCube() {
		return false;
	}

	public int getRenderType() {
		return 37;
	}

	public void incrementColor(World var1, int var2, int var3, int var4) {
		int var5 = var1.getBlockMeta(var2, var3, var4);
		var1.setBlockMeta(var2, var3, var4, (var5 + 1) % ExBlock.subTypes[this.id]);
	}
}
