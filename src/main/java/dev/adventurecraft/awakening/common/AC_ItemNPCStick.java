package dev.adventurecraft.awakening.common;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

class AC_ItemNPCStick extends Item {
	public AC_ItemNPCStick(int var1) {
		super(var1);
		this.setTexturePosition(5, 3);
		this.setRendered3d();
	}

	public boolean useOnBlock(ItemStack var1, PlayerEntity var2, World var3, int var4, int var5, int var6, int var7) {
		AC_EntityNPC var8 = new AC_EntityNPC(var3);
		var8.method_1338((double)var4 + 0.5D, (double)(var5 + 1), (double)var6 + 0.5D, var2.yaw + 180.0F, 0.0F);
		var8.field_1012 = var8.yaw;
		var3.spawnEntity(var8);
		return true;
	}

	public boolean postHit(ItemStack var1, LivingEntity var2, LivingEntity var3) {
		if(var2 instanceof AC_EntityNPC) {
			AC_GuiNPC.showUI((AC_EntityNPC)var2);
			return true;
		} else {
			return false;
		}
	}
}
