package dev.adventurecraft.awakening.common.gui;

import dev.adventurecraft.awakening.client.gui.components.AC_ValueBox;
import dev.adventurecraft.awakening.item.AC_ItemCursor;
import dev.adventurecraft.awakening.layout.IntRect;
import dev.adventurecraft.awakening.primitives.Property;
import dev.adventurecraft.awakening.primitives.TickTime;
import dev.adventurecraft.awakening.tile.entity.AC_TileEntityTimer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionButton;
import net.minecraft.client.gui.screens.Screen;

import java.text.Format;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AC_GuiTimer extends Screen {

    private AC_TileEntityTimer timer;

    private Format tickFormat;
    private AC_ValueBox<TickTime> activeTimeText;
    private AC_ValueBox<TickTime> inactiveTimeText;
    private AC_ValueBox<TickTime> delayTimeText;

    public AC_GuiTimer(AC_TileEntityTimer entity) {
        this.timer = entity;
    }

    public @Override void tick() {
        this.delayTimeText.tick();
        this.activeTimeText.tick();
        this.inactiveTimeText.tick();
    }

    private static String getTypeMsg(AC_TileEntityTimer timer) {
        return timer.isResetOnTrigger() ? "Reset Target" : "Trigger Target";
    }

    private AC_ValueBox<TickTime> tickProperty(IntRect rect, Supplier<Integer> getter, Consumer<Integer> setter) {
        var property = Property.of(getter, setter).map(TickTime::new, TickTime::ticks32);
        return new AC_ValueBox<>(rect, property, this.tickFormat);
    }

    public @Override void init() {
        this.buttons.clear();
        this.buttons.add(new OptionButton(0, 4, 40, "Use Current Selection"));
        this.buttons.add(new OptionButton(1, 4, 60, getTypeMsg(this.timer)));

        this.tickFormat = TickTime.TIME_FORMAT;

        int x = 80;
        int y = 81;
        int w = 70;
        int h = 16;
        AC_TileEntityTimer t = this.timer;
        this.delayTimeText = this.tickProperty(new IntRect(x, y, w, h), t::getTimeDelay, t::setTimeDelay);
        this.activeTimeText = this.tickProperty(new IntRect(x, y + 20, w, h), t::getTimeActive, t::setTimeActive);
        this.inactiveTimeText = this.tickProperty(new IntRect(x, y + 40, w, h), t::getTimeInactive, t::setTimeInactive);
    }

    protected @Override void buttonClicked(Button btn) {
        AC_TileEntityTimer timer = this.timer;
        if (btn.id == 0) {
            timer.setMin(AC_ItemCursor.min());
            timer.setMax(AC_ItemCursor.max());
            timer.setChanged();
        }
        else if (btn.id == 1) {
            timer.setResetOnTrigger(!timer.isResetOnTrigger());
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

            var fmt = TickTime.FULL_TIME_FORMAT;
            String timeMsg = timer.ticksDelay > 0
                ? "Delay: " + fmt.format(new TickTime(timer.ticksDelay))
                : "Time: " + fmt.format(new TickTime(timer.ticks));
            this.drawString(font, timeMsg, 4, 184, textColor);
        }

        this.drawString(font, "Delay for:", 4, 84, textColor);
        this.drawString(font, "Active for:", 4, 104, textColor);
        this.drawString(font, "Inactive for:", 4, 124, textColor);

        this.activeTimeText.render(font);
        this.inactiveTimeText.render(font);
        this.delayTimeText.render(font);

        super.render(mouseX, mouseY, deltaTime);
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
