package dev.adventurecraft.awakening.common;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;

public class AC_ItemSubtypes extends BlockItem {
	public AC_ItemSubtypes(int var1) {
		super(var1);
		this.setDurability(0);
		this.setHasSubItems(true);
	}

	public int getTexturePosition(int var1) {
		return Block.BY_ID[this.id].getTextureForSide(0, var1);
	}

	public int getMetaData(int var1) {
		return var1;
	}
}
