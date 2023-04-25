package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class AC_BlockLightBulb extends Block {
	protected AC_BlockLightBulb(int var1, int var2) {
		super(var1, var2, Material.AIR);
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

	public boolean canBeTriggered() {
		return true;
	}

	public void onTriggerActivated(World var1, int var2, int var3, int var4) {
		int var5 = var1.getBlockMeta(var2, var3, var4);
		var1.placeBlockWithMetaData(var2, var3, var4, 0, 0);
		var1.placeBlockWithMetaData(var2, var3, var4, this.id, var5);
	}

	public void onTriggerDeactivated(World var1, int var2, int var3, int var4) {
		int var5 = var1.getBlockMeta(var2, var3, var4);
		var1.placeBlockWithMetaData(var2, var3, var4, 0, 0);
		var1.placeBlockWithMetaData(var2, var3, var4, this.id, var5);
	}

	public int getBlockLightValue(BlockView var1, int var2, int var3, int var4) {
		if (((ExWorld) Minecraft.instance.world).getTriggerManager().isActivated(var2, var3, var4)) {
			return 0;
		}
		return var1.getBlockMeta(var2, var3, var4);
	}

	public void onBlockPlaced(World var1, int var2, int var3, int var4, int var5) {
		var1.setBlockMeta(var2, var3, var4, 15);
	}

	public boolean isCollidable() {
		return AC_DebugMode.active;
	}

	public boolean isFullCube() {
		return false;
	}

	public int getRenderType() {
		return 1;
	}

	public boolean canUse(World var1, int var2, int var3, int var4, PlayerEntity var5) {
		if(AC_DebugMode.active) {
			AC_GuiLightBulb.showUI(var1, var2, var3, var4);
		}
		return true;
	}
}
