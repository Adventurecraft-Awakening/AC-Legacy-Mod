package dev.adventurecraft.awakening.common;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class AC_BlockDarkness extends Block {
	protected AC_BlockDarkness(int var1, int var2) {
		super(var1, var2, Material.AIR);
		this.setLightOpacity(2);
	}

	public boolean isFullOpaque() {
		return false;
	}

	public AxixAlignedBoundingBox getCollisionShape(World var1, int var2, int var3, int var4) {
		return null;
	}

	public boolean shouldRender(BlockView var1, int var2, int var3, int var4) {
		return AC_DebugMode.active;
	}

	public boolean isCollidable() {
		return AC_DebugMode.active;
	}
}
