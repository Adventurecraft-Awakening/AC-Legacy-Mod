package dev.adventurecraft.awakening.common;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class AC_ItemCursor extends Item {
	public static boolean bothSet = false;
	public static boolean firstPosition = true;
	public static int oneX;
	public static int oneY;
	public static int oneZ;
	public static int twoX;
	public static int twoY;
	public static int twoZ;
	public static int minX;
	public static int minY;
	public static int minZ;
	public static int maxX;
	public static int maxY;
	public static int maxZ;

	protected AC_ItemCursor(int var1) {
		super(var1);
	}

	public boolean onItemUseLeftClick(ItemStack var1, PlayerEntity var2, World var3, int var4, int var5, int var6, int var7) {
		return this.useOnBlock(var1, var2, var3, var4, var5, var6, var7);
	}

	public boolean useOnBlock(ItemStack var1, PlayerEntity var2, World var3, int var4, int var5, int var6, int var7) {
		if(firstPosition) {
			Minecraft.instance.overlay.addChatMessage(String.format("Setting Cursor Position 1 (%d, %d, %d)", var4, var5, var6));
			oneX = var4;
			oneY = var5;
			oneZ = var6;
		} else {
			Minecraft.instance.overlay.addChatMessage(String.format("Setting Cursor Position 2 (%d, %d, %d)", var4, var5, var6));
			twoX = var4;
			twoY = var5;
			twoZ = var6;
			bothSet = true;
		}

		minX = Math.min(oneX, twoX);
		minY = Math.min(oneY, twoY);
		minZ = Math.min(oneZ, twoZ);
		maxX = Math.max(oneX, twoX);
		maxY = Math.max(oneY, twoY);
		maxZ = Math.max(oneZ, twoZ);
		firstPosition = !firstPosition;
		return false;
	}
}
