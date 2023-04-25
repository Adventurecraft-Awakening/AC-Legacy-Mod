package dev.adventurecraft.awakening.common;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class AC_ItemBrush extends Item {
	protected AC_ItemBrush(int var1) {
		super(var1);
	}

	public boolean useOnBlock(ItemStack var1, PlayerEntity var2, World var3, int var4, int var5, int var6, int var7) {
		Block var8 = Block.BY_ID[var3.getBlockId(var4, var5, var6)];
		if(var8 instanceof AC_IBlockColor) {
			((AC_IBlockColor)var8).incrementColor(var3, var4, var5, var6);
			var3.notifyListeners(var4, var5, var6);
		} else {
			Minecraft.instance.overlay.addChatMessage("Doesn\'t implement Color :(");
		}

		return false;
	}
}
