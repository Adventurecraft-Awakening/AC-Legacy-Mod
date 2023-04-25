package dev.adventurecraft.awakening.common;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

class AC_ItemBomb extends Item {
	public AC_ItemBomb(int var1) {
		super(var1);
		this.setTexturePosition(150);
	}

	public ItemStack use(ItemStack var1, World var2, PlayerEntity var3) {
		--var1.count;
		var2.spawnEntity(new AC_EntityBomb(var2, var3));
		return var1;
	}
}
