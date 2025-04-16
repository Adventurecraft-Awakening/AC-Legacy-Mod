package dev.adventurecraft.awakening.item;

import dev.adventurecraft.awakening.entity.AC_EntityBoomerang;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

class AC_ItemBoomerang extends Item {

	public AC_ItemBoomerang(int var1) {
		super(var1);
		this.texture(144);
		this.maxStackSize = 1;
		this.setMaxDamage(0);
		this.setStackedByData(true);
	}

	public int getIcon(int var1) {
		return var1 == 0 ? this.texture : 165;
	}

	public ItemInstance use(ItemInstance itemStack, Level world, Player playerEntity) {
		if(itemStack.getAuxValue() == 0) {
			world.addEntity(new AC_EntityBoomerang(world, playerEntity));
			itemStack.setDamage(1);
		}

		return itemStack;
	}
}
