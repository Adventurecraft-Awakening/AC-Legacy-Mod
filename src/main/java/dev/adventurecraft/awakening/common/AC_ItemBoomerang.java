package dev.adventurecraft.awakening.common;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

class AC_ItemBoomerang extends Item {
	public AC_ItemBoomerang(int var1) {
		super(var1);
		this.setTexturePosition(144);
		this.maxStackSize = 1;
		this.setDurability(0);
		this.setHasSubItems(true);
	}

	public int getTexturePosition(int var1) {
		return var1 == 0 ? this.texturePosition : 165;
	}

	public ItemStack use(ItemStack var1, World var2, PlayerEntity var3) {
		if(var1.getMeta() == 0) {
			var2.spawnEntity(new AC_EntityBoomerang(var2, var3, var1));
			var1.setMeta(1);
		}

		return var1;
	}
}
