package dev.adventurecraft.awakening.common.gui;

import dev.adventurecraft.awakening.tile.AC_Blocks;
import dev.adventurecraft.awakening.tile.entity.AC_TileEntityTimer;
import dev.adventurecraft.awakening.common.GuiSlider2;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.OptionButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.Tile;

public class AC_GuiTimer extends Screen {
    boolean ignoreNext = false;
    private AC_TileEntityTimer timer;
    boolean useTextFields;
    GuiSlider2 activeTime;
    GuiSlider2 deactiveTime;
    GuiSlider2 delayTime;
    private EditBox activeTimeText;
    private EditBox deactiveTimeText;
    private EditBox delayTimeText;

    public AC_GuiTimer(AC_TileEntityTimer entity) {
        this.timer = entity;
    }

    public void tick() {
        if (this.useTextFields) {
            this.delayTimeText.tick();
            this.activeTimeText.tick();
            this.deactiveTimeText.tick();
        }
    }

    public void init() {
        this.buttons.clear();
        this.buttons.add(new OptionButton(0, 4, 40, "Use Current Selection"));

        var var1 = new OptionButton(1, 4, 60, "Trigger Target");
        if (this.timer.resetOnTrigger) {
            var1.message = "Reset Target";
        }
        this.buttons.add(var1);

        if (!this.useTextFields) {
            this.delayTime = new GuiSlider2(4, 4, 80, 10, String.format("Delay for: %.2fs", (float) this.timer.timeDelay / 20.0F), (float) this.timer.timeDelay / 20.0F / 60.0F);
            this.buttons.add(this.delayTime);
            this.activeTime = new GuiSlider2(2, 4, 100, 10, String.format("Active for: %.2fs", (float) this.timer.timeActive / 20.0F), (float) this.timer.timeActive / 20.0F / 60.0F);
            this.buttons.add(this.activeTime);
            this.deactiveTime = new GuiSlider2(3, 4, 120, 10, String.format("Deactive for: %.2fs", (float) this.timer.timeDeactive / 20.0F), (float) this.timer.timeDeactive / 20.0F / 60.0F);
            this.buttons.add(this.deactiveTime);
        } else {
            this.delayTimeText = new EditBox(this, this.font, 80, 81, 70, 16, String.format("%.2f", (float) this.timer.timeDelay / 20.0F));
            this.activeTimeText = new EditBox(this, this.font, 80, 101, 70, 16, String.format("%.2f", (float) this.timer.timeActive / 20.0F));
            this.deactiveTimeText = new EditBox(this, this.font, 80, 121, 70, 16, String.format("%.2f", (float) this.timer.timeDeactive / 20.0F));
        }

        this.buttons.add(new OptionButton(5, 4, 140, "Switch Input Mode"));
    }

    protected void buttonClicked(Button btn) {
        AC_TileEntityTimer timer = this.timer;
        if (btn.id == 0) {
            int id = timer.level.getTile(timer.x, timer.y, timer.z);
            if (id == AC_Blocks.timer.id) {
                AC_Blocks.timer.setTriggerToSelection(timer.level, timer.x, timer.y, timer.z);
            }
        } else if (btn.id == 1) {
            timer.resetOnTrigger = !timer.resetOnTrigger;
            if (timer.resetOnTrigger) {
                btn.message = "Reset Target";
            } else {
                btn.message = "Trigger Target";
            }
        } else if (btn.id == 5 && !this.ignoreNext) {
            this.useTextFields = !this.useTextFields;
            this.init();
            this.ignoreNext = true;
        }
    }

    public void render(int var1, int var2, float var3) {
        this.ignoreNext = false;
        this.fill(0, 0, this.width, this.height, Integer.MIN_VALUE);
        AC_TileEntityTimer timer = this.timer;

        int textColor = 14737632;
        this.drawString(this.font, String.format("Min: (%d, %d, %d)", timer.minX, timer.minY, timer.minZ), 4, 4, textColor);
        this.drawString(this.font, String.format("Max: (%d, %d, %d)", timer.maxX, timer.maxY, timer.maxZ), 4, 24, textColor);

        if (!timer.active && timer.canActivate) {
            this.drawString(this.font, "State: Ready", 4, 164, textColor);
        } else {
            if (timer.active) {
                this.drawString(this.font, "State: Active", 4, 164, textColor);
            } else if (!timer.canActivate) {
                this.drawString(this.font, "State: Deactive", 4, 164, textColor);
            }

            if (this.timer.ticksDelay > 0) {
                this.drawString(this.font, String.format("Delay: %.2f", timer.ticksDelay * 0.05F), 4, 184, textColor);
            } else {
                this.drawString(this.font, String.format("Time: %.2f", timer.ticks * 0.05F), 4, 184, textColor);
            }
        }

        if (!this.useTextFields) {
            this.timer.timeActive = (int) (this.activeTime.sliderValue * 60.0F * 20.0F);
            this.timer.timeDeactive = (int) (this.deactiveTime.sliderValue * 60.0F * 20.0F);
            this.timer.timeDelay = (int) (this.delayTime.sliderValue * 60.0F * 20.0F);

            this.delayTime.message = String.format("Delay for: %.2fs", timer.timeDelay / 20.0F);
            this.activeTime.message = String.format("Active for: %.2fs", timer.timeActive / 20.0F);
            this.deactiveTime.message = String.format("Deactive for: %.2fs", timer.timeDeactive / 20.0F);
        } else {
            this.drawString(this.font, "Delay For:", 4, 84, textColor);
            this.drawString(this.font, "Active For:", 4, 104, textColor);
            this.drawString(this.font, "Deactive For:", 4, 124, textColor);

            this.activeTimeText.render();
            this.deactiveTimeText.render();
            this.delayTimeText.render();

            try {
                float var4 = Float.parseFloat(this.activeTimeText.getValue());
                timer.timeActive = (int) (var4 * 20.0F);
            } catch (NumberFormatException ignored) {
            }

            try {
                float var4 = Float.parseFloat(this.deactiveTimeText.getValue());
                timer.timeDeactive = (int) (var4 * 20.0F);
            } catch (NumberFormatException ignored) {
            }

            try {
                float var4 = Float.parseFloat(this.delayTimeText.getValue());
                timer.timeDelay = (int) (var4 * 20.0F);
            } catch (NumberFormatException ignored) {
            }

            timer.setChanged();
        }

        super.render(var1, var2, var3);
    }

    protected void keyPressed(char ch, int key) {
        if (this.useTextFields) {
            boolean isMod = key == 14 || ch >= 48 && ch <= 57 || ch == 46 || ch == 9;

            if (this.activeTimeText.active && isMod) {
                this.activeTimeText.charTyped(ch, key);
            }

            if (this.deactiveTimeText.active && isMod) {
                this.deactiveTimeText.charTyped(ch, key);
            }

            if (this.delayTimeText.active && isMod) {
                this.delayTimeText.charTyped(ch, key);
            }
        }

        super.keyPressed(ch, key);
    }

    protected void mouseClicked(int x, int y, int button) {
        if (this.useTextFields) {
            this.delayTimeText.clicked(x, y, button);
            this.activeTimeText.clicked(x, y, button);
            this.deactiveTimeText.clicked(x, y, button);
        }

        super.mouseClicked(x, y, button);
    }

    public static void showUI(AC_TileEntityTimer entity) {
        Minecraft.instance.setScreen(new AC_GuiTimer(entity));
    }

    public boolean isPauseScreen() {
        return false;
    }
}
