package dev.adventurecraft.awakening.common.gui;

import dev.adventurecraft.awakening.editor.EditorSession;
import dev.adventurecraft.awakening.client.render.compat.LegacyPassBridge;
import dev.adventurecraft.awakening.extension.client.render.entity.ExItemRenderer;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.level.tile.Tile;

class AC_GuiEditPalette extends GuiComponent {
	int numRows;
	List controlList = new ArrayList();
	ArrayList blocks = new ArrayList();
	Button selectedButton;
	ItemInstance item = new ItemInstance(0, 0, 0);
	private static ItemRenderer itemRenderer = new ItemRenderer();
	static int rows = 8;
	static int columns = 4;
	static int scrollHeight = 8;
	float scrollPosition = 0.0F;

	AC_GuiEditPalette() {
		this.filterBlocks(0);
	}

	void filterBlocks(int var1) {
		this.blocks.clear();

		for(int var2 = 0; var2 < 255; ++var2) {
			if(Tile.tiles[var2] != null) {
				this.blocks.add(Tile.tiles[var2]);
			}
		}

		this.numRows = (this.blocks.size() + columns - 1) / columns;
	}

	boolean mouseClicked(int var1, int var2, int var3, Minecraft var4, int var5, int var6) {
		if(var3 == 0) {
			byte var7 = 0;
			int var8 = var6 / 2 - rows * 8;
			int var9 = (var1 - var7) / 16;
			int var10 = (var2 - var8) / 16;
			if(var10 < rows && var10 >= 0) {
				int var11;
				if(var9 < columns && var9 >= 0) {
					var4.soundEngine.playUI("random.click", 1.0F, 1.0F);
					var11 = var9 + var10 * columns;
					if(var11 + this.getOffset() < this.blocks.size()) {
						EditorSession.current().mapEditing.setBlock(((Tile)this.blocks.get(var11 + this.getOffset())).id, 0);
						return true;
					}
				} else if(var9 == columns && var1 % 16 < 4 && this.needScrollbar()) {
					var11 = var2 - var8 - scrollHeight / 2;
					this.scrollPosition = Math.max(Math.min((float)var11 / ((float)rows * 16.0F - (float)scrollHeight), 1.0F), 0.0F);
					return true;
				}
			}
		}

		return false;
	}

	void drawPalette(Minecraft var1, Font var2, int var3, int var4) {
		byte var5 = 0;
		int var6 = 16 * columns;
		int var7 = var4 / 2 - rows * 8;
		int var8 = rows * 16;
		if(this.needScrollbar()) {
			var6 += 4;
		}

		this.fill(var5, var7, var5 + var6, var7 + var8, Integer.MIN_VALUE);
		int var9;
		if(this.needScrollbar()) {
			var9 = (int)(this.scrollPosition * (float)(var8 - scrollHeight));
			this.fill(var5 + var6 - 4, var7 + var9, var5 + var6, var7 + var9 + scrollHeight, -2130706433);
		}

        try (var ui = LegacyPassBridge.uiContext(var2, var1.textures)) {
            var preview = (ExItemRenderer) itemRenderer;
            var9 = this.getOffset();
            for (int index = 0; index < rows * columns && index + var9 < this.blocks.size(); index++) {
                this.item.id = ((Tile) this.blocks.get(index + var9)).id;
                preview.renderGuiItem(ui, this.item, index % columns * 16, var4 / 2 - rows * 8 + 16 * (index / columns));
            }
        }
	}

	private int getOffset() {
		return columns * (int)((double)(this.scrollPosition * (float)(this.numRows - rows)) + 0.5D);
	}

	private boolean needScrollbar() {
		return this.numRows > rows;
	}
}
