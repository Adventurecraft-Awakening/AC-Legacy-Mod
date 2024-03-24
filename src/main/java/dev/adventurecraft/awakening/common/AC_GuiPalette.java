package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.client.ExInteractionManager;
import net.minecraft.client.gui.screen.container.DoubleChestScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.inventory.Inventory;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class AC_GuiPalette extends DoubleChestScreen {

    private InventoryDebug palette;
    GuiSlider2 extraWidth;
    GuiSlider2 extraDepth;
    private ButtonWidget prevButton;
    private ButtonWidget nextPageButton;
    private ButtonWidget previousPageButton;

    public AC_GuiPalette(Inventory playerInventory, InventoryDebug chestInventory) {
        super(playerInventory, chestInventory);
        this.palette = chestInventory;
    }

    @Override
    public void initVanillaScreen() {
        super.initVanillaScreen();
        var intMan = (ExInteractionManager) this.client.interactionManager;

        int sliderID = 50; // Same for both for some reason

        int slidersY = 3; // 3 units from the top
        int containerMiddle = this.width / 2;

        // Source is 2 units right from the middle
        int extraDepthSliderX = containerMiddle + 2;
        // Source is 2 units and a whole length from the middle, essentially anchoring to the right instead of left.
        int extraWidthSliderX = containerMiddle - 2 - GuiSlider2.DEFAULT_WIDTH;

        // Current config values
        int destroyExtraDepth = intMan.getDestroyExtraDepth();
        int destroyExtraWidth = intMan.getDestroyExtraWidth();

        this.extraDepth = new GuiSlider2(sliderID, extraDepthSliderX, slidersY, 10, String.format("Extra Depth: %d", destroyExtraDepth), (float) destroyExtraDepth / 16.0F);
        this.extraWidth = new GuiSlider2(sliderID, extraWidthSliderX, slidersY, 10, String.format("Extra Width: %d", destroyExtraWidth), (float) destroyExtraWidth / 5.0F);

        int buttonId = 50;
        int buttonsX = this.width / 2 + containerWidth / 2 + 4;
        int heightMiddle = this.height / 2;
        this.nextPageButton = new ButtonWidget(buttonId, buttonsX, heightMiddle + 30, 50, 20, "Next");
        this.previousPageButton = new ButtonWidget(buttonId, buttonsX, heightMiddle, 50, 20, "Previous");

        this.buttons.add(this.extraDepth);
        this.buttons.add(this.extraWidth);

        this.buttons.add(this.nextPageButton);
        this.buttons.add(this.previousPageButton);


    }

    @Override
    protected void keyPressed(char keyCharacter, int keyCode) {
        super.keyPressed(keyCharacter, keyCode);
        if (keyCode == Keyboard.KEY_F7) {
            goToPreviousPage();
        } else if (keyCode == Keyboard.KEY_F8) {
            goToNextPage();
        }
    }

    private void goToPreviousPage() {
        if (this.palette.firstItem > 1) {
            this.palette.fillInventoryBackwards(this.palette.firstItem - 1);
        }
    }

    private void goToNextPage() {
        if (!this.palette.atEnd) {
            this.palette.fillInventory(this.palette.lastItem + 1);
        }
    }

    @Override
    public void render(int var1, int var2, float var3) {
        super.render(var1, var2, var3);
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        for (ButtonWidget button : (List<ButtonWidget>) this.buttons) {
            button.render(this.client, var1, var2);
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

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0) {
            for (ButtonWidget button : (List<ButtonWidget>) this.buttons) {
                if (button.isMouseOver(this.client, mouseX, mouseY)) {
                    this.prevButton = button;
                    this.client.soundHelper.playSound("random.click", 1.0F, 1.0F);
                }
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void buttonClicked(ButtonWidget buttonWidget) {
        if (buttonWidget == this.nextPageButton) {
            goToNextPage();
        } else if (buttonWidget == this.previousPageButton) {
            goToPreviousPage();
        } else {
            super.buttonClicked(buttonWidget);
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        if (this.prevButton != null && mouseButton == 0) {
            this.prevButton.mouseReleased(mouseX, mouseY);
            this.prevButton = null;
        }

        super.mouseReleased(mouseX, mouseY, mouseButton);
    }
}
