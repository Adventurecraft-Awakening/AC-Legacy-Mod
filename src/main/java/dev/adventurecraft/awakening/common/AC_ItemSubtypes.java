package dev.adventurecraft.awakening.common;

import net.minecraft.world.item.TileItem;
import net.minecraft.world.level.tile.Tile;

public class AC_ItemSubtypes extends TileItem {
	public AC_ItemSubtypes(int var1) {
		super(var1);
		this.setMaxDamage(0);
		this.setStackedByData(true);
	}

	public int getIcon(int var1) {
		return Tile.tiles[this.id].getTexture(0, var1);
	}

	public int getLevelDataForAuxValue(int var1) {
		return var1;
	}
}
