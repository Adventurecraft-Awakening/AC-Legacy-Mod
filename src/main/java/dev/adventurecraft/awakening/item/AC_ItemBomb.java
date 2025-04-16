package dev.adventurecraft.awakening.item;

import dev.adventurecraft.awakening.entity.AC_EntityBomb;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

class AC_ItemBomb extends Item {
	public AC_ItemBomb(int var1) {
		super(var1);
		this.texture(150);
	}

	public ItemInstance use(ItemInstance stack, Level world, Player player) {
		--stack.count;
		world.addEntity(new AC_EntityBomb(world, player));
		return stack;
	}
}
