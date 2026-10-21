package dev.adventurecraft.awakening.common.gui;

import dev.adventurecraft.awakening.client.render.UiDrawContext;
import dev.adventurecraft.awakening.common.GuiSlider2;
import dev.adventurecraft.awakening.common.InventoryDebug;
import dev.adventurecraft.awakening.common.ScrollableContainer;
import dev.adventurecraft.awakening.common.ScrollableContainerScreen;
import dev.adventurecraft.awakening.extension.client.ExInteractionManager;
import dev.adventurecraft.awakening.util.MathF;
import org.lwjgl.input.Keyboard;

import net.minecraft.client.gui.components.Button;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;

public class AC_GuiPalette extends ScrollableContainerScreen {

    private final Container inv1;
    private final Container inv2;
    private final int slotHeight;

    private GuiSlider2 extraWidth;
    private GuiSlider2 extraDepth;
    private Button nextPageButton;
    private Button previousPageButton;

    public AC_GuiPalette(Container playerInventory, InventoryDebug chestInventory, int slotHeight, int rowsPerPage) {
        super(new ScrollableContainer(playerInventory, chestInventory, slotHeight), rowsPerPage);
        this.inv1 = playerInventory;
        this.inv2 = chestInventory;
        this.slotHeight = slotHeight;

        int yOffset = (this.rowsPerPage - 4) * slotHeight;
        for (Slot slot : container.getStaticSlots()) {
            slot.z += yOffset;
        }

        this.passEvents = false;
        int n = 222;
        int n2 = n - 108;
        this.containerHeight = n2 + this.rowsPerPage * slotHeight;
    }

    @Override
    public void init() {
        super.init();
        var intMan = (ExInteractionManager) this.minecraft.gameMode;

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
        this.nextPageButton = new Button(buttonId, buttonsX, heightMiddle + 30, 50, 20, "Next");
        this.previousPageButton = new Button(buttonId, buttonsX, heightMiddle, 50, 20, "Previous");

        this.buttons.add(this.extraDepth);
        this.buttons.add(this.extraWidth);

        this.buttons.add(this.nextPageButton);
        this.buttons.add(this.previousPageButton);
    }

    @Override
    protected void keyPressed(char keyCharacter, int keyCode) {
        if (keyCode == Keyboard.KEY_F7) {
            this.minecraft.soundEngine.playUI("random.click", 1.0F, 1.0F);
            goToPageRelative(-1);
        } else if (keyCode == Keyboard.KEY_F8) {
            this.minecraft.soundEngine.playUI("random.click", 1.0F, 1.0F);
            goToPageRelative(1);
        } else {
            super.keyPressed(keyCharacter, keyCode);
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int button) {
        if (this.clickedButton != null && button == 0) {
            this.clickedButton.mouseReleased(mouseX, mouseY);
            this.clickedButton = null;
        }
    }

    private void goToPageRelative(int count) {
        double row = MathF.roundToZero(this.itemList.getScrollRow() / this.rowsPerPage);
        double newRow = (row + count) * this.rowsPerPage;
        this.itemList.setScrollRow(newRow, false);
    }

    @Override
    public void render(int mouseX, int mouseY, float deltaTime) {
        super.render(mouseX, mouseY, deltaTime);
        var intMan = (ExInteractionManager) this.minecraft.gameMode;
        intMan.setDestroyExtraDepth((int) Math.min(16.0F * this.extraDepth.sliderValue, 15.0F));
        intMan.setDestroyExtraWidth((int) Math.min(5.0F * this.extraWidth.sliderValue, 4.0F));
        this.extraWidth.message = String.format("Extra Width: %d", intMan.getDestroyExtraWidth());
        this.extraDepth.message = String.format("Extra Depth: %d", intMan.getDestroyExtraDepth());
    }

    @Override
    protected void buttonClicked(Button buttonWidget) {
        if (buttonWidget == this.nextPageButton) {
            goToPageRelative(1);
        } else if (buttonWidget == this.previousPageButton) {
            goToPageRelative(-1);
        } else {
            super.buttonClicked(buttonWidget);
        }
    }

    @Override
    protected void renderForeground(UiDrawContext ui) {
        ui.text(this.inv2.getName(), 8, 6, 0xff404040, false);
        ui.text(this.inv1.getName(), 8, this.containerHeight - 96 + 2, 0xff404040, false);
    }

    protected void renderContainerBackground(float f) {
        int x = (this.width - this.containerWidth) / 2;
        int y = (this.height - this.containerHeight) / 2;
        try (var ui = dev.adventurecraft.awakening.client.render.compat.LegacyPassBridge.uiContext(this.font, this.minecraft.textures)) {
            var texture = ui.texture("/gui/container.png");
            int topHeight = this.rowsPerPage * slotHeight + 17;
            var color = new dev.adventurecraft.awakening.layout.IntCorner(-1);
            ui.image(texture, new dev.adventurecraft.awakening.layout.Rect(x, y, this.containerWidth, topHeight),
                new dev.adventurecraft.awakening.layout.Rect(0, 0, this.containerWidth / 256f, topHeight / 256f), color);
            ui.image(texture, new dev.adventurecraft.awakening.layout.Rect(x, y + topHeight, this.containerWidth, 96),
                new dev.adventurecraft.awakening.layout.Rect(0, 126 / 256f, this.containerWidth / 256f, 96 / 256f), color);
        }
    }
}
