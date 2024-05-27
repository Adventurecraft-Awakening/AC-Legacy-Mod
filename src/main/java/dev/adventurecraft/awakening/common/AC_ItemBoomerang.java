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

	public ItemStack use(ItemStack itemStack, World world, PlayerEntity playerEntity) {
		if(itemStack.getMeta() == 0) {
			world.spawnEntity(new AC_EntityBoomerang(world, playerEntity));
			itemStack.setMeta(1);
		}

		return itemStack;
	}
}
