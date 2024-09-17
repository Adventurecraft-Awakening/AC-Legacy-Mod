package dev.adventurecraft.awakening.common;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import org.lwjgl.opengl.GL11;

public class GuiSlider2 extends Button {

    public static int DEFAULT_WIDTH = 150;
    public static int DEFAULT_HEIGHT = 20;
	public float sliderValue;
	public boolean dragging = false;

	public GuiSlider2(int buttonID, int xPosition, int yPosition, int var4, String var5, float sliderValue) {
		super(buttonID, xPosition, yPosition, DEFAULT_WIDTH, DEFAULT_HEIGHT, var5);
		this.sliderValue = sliderValue;
	}

	protected int getYImage(boolean var1) {
		return 0;
	}

	protected void renderBg(Minecraft var1, int var2, int var3) {
		if(this.visible) {
			if(this.dragging) {
				this.sliderValue = (float)(var2 - (this.x + 4)) / (float)(this.width - 8);
				if(this.sliderValue < 0.0F) {
					this.sliderValue = 0.0F;
				}

				if(this.sliderValue > 1.0F) {
					this.sliderValue = 1.0F;
				}
			}

			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.blit(this.x + (int)(this.sliderValue * (float)(this.width - 8)), this.y, 0, 66, 4, 20);
			this.blit(this.x + (int)(this.sliderValue * (float)(this.width - 8)) + 4, this.y, 196, 66, 4, 20);
		}
	}

	public boolean isMouseOver(Minecraft var1, int var2, int var3) {
		if(super.isMouseOver(var1, var2, var3)) {
			this.sliderValue = (float)(var2 - (this.x + 4)) / (float)(this.width - 8);
			if(this.sliderValue < 0.0F) {
				this.sliderValue = 0.0F;
			}

			if(this.sliderValue > 1.0F) {
				this.sliderValue = 1.0F;
			}

			this.dragging = true;
			return true;
		} else {
			return false;
		}
	}

	public void mouseReleased(int var1, int var2) {
		this.dragging = false;
	}
}
