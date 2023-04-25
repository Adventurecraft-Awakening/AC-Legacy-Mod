package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.client.ExInteractionManager;
import net.minecraft.client.gui.screen.container.DoubleChestScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.inventory.Inventory;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class AC_GuiPalette extends DoubleChestScreen {
    private InventoryDebug palette;
    GuiSlider2 extraWidth;
    GuiSlider2 extraDepth;
    private ButtonWidget a;

    public AC_GuiPalette(Inventory var1, InventoryDebug var2) {
        super(var1, var2);
        this.palette = var2;
    }

    public void initVanillaScreen() {
        super.initVanillaScreen();
        ExInteractionManager intMan = (ExInteractionManager) this.client.interactionManager;
        this.extraDepth = new GuiSlider2(50, this.width / 2 + 2, 3, 10, String.format("Extra Depth: %d", intMan.getDestroyExtraDepth()), (float) intMan.getDestroyExtraDepth() / 16.0F);
        this.extraWidth = new GuiSlider2(50, this.width / 2 - 2 - this.extraDepth.width, 3, 10, String.format("Extra Width: %d", intMan.getDestroyExtraWidth()), (float) intMan.getDestroyExtraWidth() / 5.0F);
        this.buttons.add(this.extraDepth);
        this.buttons.add(this.extraWidth);
    }

    protected void keyPressed(char var1, int var2) {
        super.keyPressed(var1, var2);
        if (var2 == 65 && this.palette.firstItem > 1) {
            this.palette.fillInventoryBackwards(this.palette.firstItem - 1);
        } else if (var2 == 66 && !this.palette.atEnd) {
            this.palette.fillInventory(this.palette.lastItem + 1);
        }
    }

    public void render(int var1, int var2, float var3) {
        super.render(var1, var2, var3);
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        for (ButtonWidget var5 : (List<ButtonWidget>) this.buttons) {
            var5.render(this.client, var1, var2);
        }

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glPopMatrix();
        ExInteractionManager intMan = (ExInteractionManager) this.client.interactionManager;
        intMan.setDestroyExtraDepth((int) Math.min(16.0F * this.extraDepth.sliderValue, 15.0F));
        intMan.setDestroyExtraWidth((int) Math.min(5.0F * this.extraWidth.sliderValue, 4.0F));
        this.extraWidth.text = String.format("Extra Width: %d", intMan.getDestroyExtraWidth());
        this.extraDepth.text = String.format("Extra Depth: %d", intMan.getDestroyExtraDepth());
    }

    protected void mouseClicked(int var1, int var2, int var3) {
        if (var3 == 0) {
            for (ButtonWidget var5 : (List<ButtonWidget>) this.buttons) {
                if (var5.isMouseOver(this.client, var1, var2)) {
                    this.a = var5;
                    this.client.soundHelper.playSound("random.click", 1.0F, 1.0F);
                    this.buttonClicked(var5);
                }
            }
        }

        super.mouseClicked(var1, var2, var3);
    }

    protected void mouseReleased(int var1, int var2, int var3) {
        if (this.a != null && var3 == 0) {
            this.a.mouseReleased(var1, var2);
            this.a = null;
        }

        super.mouseReleased(var1, var2, var3);
    }
}
