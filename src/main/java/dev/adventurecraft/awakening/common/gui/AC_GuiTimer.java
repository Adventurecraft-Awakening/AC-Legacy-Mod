package dev.adventurecraft.awakening.common.gui;

import dev.adventurecraft.awakening.tile.AC_Blocks;
import dev.adventurecraft.awakening.common.AC_TileEntityTimer;
import dev.adventurecraft.awakening.common.GuiSlider2;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.OptionButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.level.Level;

public class AC_GuiTimer extends Screen {
    boolean ignoreNext = false;
    private AC_TileEntityTimer timer;
    private int blockX;
    private int blockY;
    private int blockZ;
    private Level world;
    boolean useTextFields;
    GuiSlider2 activeTime;
    GuiSlider2 deactiveTime;
    GuiSlider2 delayTime;
    private EditBox activeTimeText;
    private EditBox deactiveTimeText;
    private EditBox delayTimeText;

    public AC_GuiTimer(Level var1, int var2, int var3, int var4, AC_TileEntityTimer var5) {
        this.world = var1;
        this.blockX = var2;
        this.blockY = var3;
        this.blockZ = var4;
        this.timer = var5;
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
        OptionButton var1 = new OptionButton(1, 4, 60, "Trigger Target");
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

    protected void buttonClicked(Button var1) {
        if (var1.id == 0) {
            int var2 = this.world.getTile(this.blockX, this.blockY, this.blockZ);
            if (var2 == AC_Blocks.timer.id) {
                AC_Blocks.timer.setTriggerToSelection(this.world, this.blockX, this.blockY, this.blockZ);
            }
        } else if (var1.id == 1) {
            this.timer.resetOnTrigger = !this.timer.resetOnTrigger;
            if (this.timer.resetOnTrigger) {
                var1.message = "Reset Target";
            } else {
                var1.message = "Trigger Target";
            }
        } else if (var1.id == 5 && !this.ignoreNext) {
            this.useTextFields = !this.useTextFields;
            this.init();
            this.ignoreNext = true;
        }

    }

    public void render(int var1, int var2, float var3) {
        this.ignoreNext = false;
        this.fill(0, 0, this.width, this.height, Integer.MIN_VALUE);
        this.drawString(this.font, String.format("Min: (%d, %d, %d)", this.timer.minX, this.timer.minY, this.timer.minZ), 4, 4, 14737632);
        this.drawString(this.font, String.format("Max: (%d, %d, %d)", this.timer.maxX, this.timer.maxY, this.timer.maxZ), 4, 24, 14737632);
        if (!this.timer.active && this.timer.canActivate) {
            this.drawString(this.font, "State: Ready", 4, 164, 14737632);
        } else {
            if (this.timer.active) {
                this.drawString(this.font, "State: Active", 4, 164, 14737632);
            } else if (!this.timer.canActivate) {
                this.drawString(this.font, "State: Deactive", 4, 164, 14737632);
            }

            if (this.timer.ticksDelay > 0) {
                this.drawString(this.font, String.format("Delay: %.2f", (float) this.timer.ticksDelay * 0.05F), 4, 184, 14737632);
            } else {
                this.drawString(this.font, String.format("Time: %.2f", (float) this.timer.ticks * 0.05F), 4, 184, 14737632);
            }
        }

        if (!this.useTextFields) {
            this.timer.timeActive = (int) (this.activeTime.sliderValue * 60.0F * 20.0F);
            this.timer.timeDeactive = (int) (this.deactiveTime.sliderValue * 60.0F * 20.0F);
            this.timer.timeDelay = (int) (this.delayTime.sliderValue * 60.0F * 20.0F);
            this.delayTime.message = String.format("Delay for: %.2fs", (float) this.timer.timeDelay / 20.0F);
            this.activeTime.message = String.format("Active for: %.2fs", (float) this.timer.timeActive / 20.0F);
            this.deactiveTime.message = String.format("Deactive for: %.2fs", (float) this.timer.timeDeactive / 20.0F);
        } else {
            this.drawString(this.font, "Delay For:", 4, 84, 14737632);
            this.drawString(this.font, "Active For:", 4, 104, 14737632);
            this.drawString(this.font, "Deactive For:", 4, 124, 14737632);
            this.activeTimeText.render();
            this.deactiveTimeText.render();
            this.delayTimeText.render();

            Float var4;
            try {
                var4 = Float.valueOf(this.activeTimeText.getValue());
                if (var4 != null) {
                    this.timer.timeActive = (int) (var4.floatValue() * 20.0F);
                }
            } catch (NumberFormatException var7) {
            }

            try {
                var4 = Float.valueOf(this.deactiveTimeText.getValue());
                if (var4 != null) {
                    this.timer.timeDeactive = (int) (var4.floatValue() * 20.0F);
                }
            } catch (NumberFormatException var6) {
            }

            try {
                var4 = Float.valueOf(this.delayTimeText.getValue());
                if (var4 != null) {
                    this.timer.timeDelay = (int) (var4.floatValue() * 20.0F);
                }
            } catch (NumberFormatException var5) {
            }
        }

        this.world.getChunkAt(this.blockX, this.blockZ).markUnsaved();
        super.render(var1, var2, var3);
    }

    protected void keyPressed(char var1, int var2) {
        if (this.useTextFields) {
            if (this.activeTimeText.active && (var2 == 14 || var1 >= 48 && var1 <= 57 || var1 == 46 || var1 == 9)) {
                this.activeTimeText.charTyped(var1, var2);
            }

            if (this.deactiveTimeText.active && (var2 == 14 || var1 >= 48 && var1 <= 57 || var1 == 46 || var1 == 9)) {
                this.deactiveTimeText.charTyped(var1, var2);
            }

            if (this.delayTimeText.active && (var2 == 14 || var1 >= 48 && var1 <= 57 || var1 == 46 || var1 == 9)) {
                this.delayTimeText.charTyped(var1, var2);
            }
        }

        super.keyPressed(var1, var2);
    }

    protected void mouseClicked(int var1, int var2, int var3) {
        if (this.useTextFields) {
            this.delayTimeText.clicked(var1, var2, var3);
            this.activeTimeText.clicked(var1, var2, var3);
            this.deactiveTimeText.clicked(var1, var2, var3);
        }

        super.mouseClicked(var1, var2, var3);
    }

    public static void showUI(Level var0, int var1, int var2, int var3, AC_TileEntityTimer var4) {
        Minecraft.instance.setScreen(new AC_GuiTimer(var0, var1, var2, var3, var4));
    }

    public boolean isPauseScreen() {
        return false;
    }
}
