package dev.adventurecraft.awakening.common.gui;

import dev.adventurecraft.awakening.client.gui.components.AC_EditBox;
import dev.adventurecraft.awakening.image.Rgba;
import dev.adventurecraft.awakening.item.AC_ItemCursor;
import dev.adventurecraft.awakening.layout.IntRect;
import dev.adventurecraft.awakening.tile.entity.AC_TileEntityTimer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionButton;
import net.minecraft.client.gui.screens.Screen;

public class AC_GuiTimer extends Screen {

    private AC_TileEntityTimer timer;
    private AC_EditBox activeTimeText;
    private AC_EditBox inactiveTimeText;
    private AC_EditBox delayTimeText;

    public AC_GuiTimer(AC_TileEntityTimer entity) {
        this.timer = entity;
    }

    public @Override void tick() {
        this.delayTimeText.tick();
        this.activeTimeText.tick();
        this.inactiveTimeText.tick();
    }

    private static String getTypeMsg(AC_TileEntityTimer timer) {
        return timer.resetOnTrigger ? "Reset Target" : "Trigger Target";
    }

    public @Override void init() {
        this.buttons.clear();
        this.buttons.add(new OptionButton(0, 4, 40, "Use Current Selection"));
        this.buttons.add(new OptionButton(1, 4, 60, getTypeMsg(this.timer)));

        this.delayTimeText = new AC_EditBox(
            new IntRect(80, 81, 70, 16),
            String.format("%.2f", (float) this.timer.timeDelay / 20.0F)
        );
        this.activeTimeText = new AC_EditBox(
            new IntRect(80, 101, 70, 16),
            String.format("%.2f", (float) this.timer.timeActive / 20.0F)
        );
        this.inactiveTimeText = new AC_EditBox(
            new IntRect(80, 121, 70, 16),
            String.format("%.2f", (float) this.timer.timeInactive / 20.0F)
        );
    }

    protected @Override void buttonClicked(Button btn) {
        AC_TileEntityTimer timer = this.timer;
        if (btn.id == 0) {
            timer.setMin(AC_ItemCursor.min());
            timer.setMax(AC_ItemCursor.max());
        }
        else if (btn.id == 1) {
            timer.resetOnTrigger = !timer.resetOnTrigger;
            btn.message = getTypeMsg(timer);
        }
    }

    public @Override void render(int mouseX, int mouseY, float deltaTime) {
        this.fill(0, 0, this.width, this.height, Integer.MIN_VALUE);

        AC_TileEntityTimer timer = this.timer;
        Font font = this.font;
        int textColor = 0xe0e0e0;

        AC_GuiStrings.drawMinMax(this, timer, 4, 4, textColor);

        if (!timer.active && timer.canActivate) {
            this.drawString(font, "State: Ready", 4, 164, textColor);
        }
        else {
            String stateMsg = timer.active ? "State: Active" : "State: Inactive";
            this.drawString(font, stateMsg, 4, 164, textColor);

            String timeMsg = timer.ticksDelay > 0
                ? String.format("Delay: %.2f", timer.ticksDelay * 0.05F)
                : String.format("Time: %.2f", timer.ticks * 0.05F);
            this.drawString(font, timeMsg, 4, 184, textColor);
        }

        this.drawString(font, "Delay for:", 4, 84, textColor);
        this.drawString(font, "Active for:", 4, 104, textColor);
        this.drawString(font, "Inactive for:", 4, 124, textColor);

        timer.timeActive = this.parseFloat(this.activeTimeText, timer.timeActive);
        timer.timeInactive = this.parseFloat(this.inactiveTimeText, timer.timeInactive);
        timer.timeDelay = this.parseFloat(this.delayTimeText, timer.timeDelay);

        this.activeTimeText.render(font);
        this.inactiveTimeText.render(font);
        this.delayTimeText.render(font);

        super.render(mouseX, mouseY, deltaTime);
    }

    private int parseFloat(AC_EditBox box, int previousValue) {
        box.resetTextColor();
        try {
            float raw = Float.parseFloat(box.toString());
            int value = (int) (raw * 20.0F);
            if (value != previousValue) {
                this.timer.setChanged();
            }
            return value;
        }
        catch (NumberFormatException ignored) {
        }
        box.setActiveTextColor(Rgba.fromRgb8(0xff, 0, 0));
        box.setInactiveTextColor(Rgba.fromRgb8(0x8f, 0, 0));
        return previousValue;
    }

    protected @Override void keyPressed(char ch, int key) {
        this.activeTimeText.charTyped(ch, key);
        this.inactiveTimeText.charTyped(ch, key);
        this.delayTimeText.charTyped(ch, key);

        super.keyPressed(ch, key);
    }

    protected @Override void mouseClicked(int x, int y, int button) {
        this.delayTimeText.clicked(x, y, button);
        this.activeTimeText.clicked(x, y, button);
        this.inactiveTimeText.clicked(x, y, button);

        super.mouseClicked(x, y, button);
    }

    public static void showUI(AC_TileEntityTimer entity) {
        Minecraft.instance.setScreen(new AC_GuiTimer(entity));
    }

    public @Override boolean isPauseScreen() {
        return false;
    }
}
