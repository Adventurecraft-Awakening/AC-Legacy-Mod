package dev.adventurecraft.awakening.item;

import dev.adventurecraft.awakening.entity.AC_EntityNPC;
import dev.adventurecraft.awakening.common.gui.AC_GuiNPC;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

class AC_ItemNPCStick extends Item {
	public AC_ItemNPCStick(int var1) {
		super(var1);
		this.setIcon(5, 3);
		this.handEquipped();
	}

	public boolean useOn(ItemInstance var1, Player var2, Level var3, int var4, int var5, int var6, int var7) {
		AC_EntityNPC var8 = new AC_EntityNPC(var3);
		var8.absMoveTo((double)var4 + 0.5D, (double)(var5 + 1), (double)var6 + 0.5D, var2.yRot + 180.0F, 0.0F);
		var8.yHeadRot = var8.yRot;
		var3.addEntity(var8);
		return true;
	}

	public boolean hurtEnemy(ItemInstance var1, LivingEntity var2, LivingEntity var3) {
		if(var2 instanceof AC_EntityNPC) {
			AC_GuiNPC.showUI((AC_EntityNPC)var2);
			return true;
		} else {
			return false;
		}
	}
}
