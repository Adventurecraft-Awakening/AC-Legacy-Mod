package dev.adventurecraft.awakening.client.gui.components;

import dev.adventurecraft.awakening.common.ClipboardHandler;
import dev.adventurecraft.awakening.layout.IntBorder;
import dev.adventurecraft.awakening.layout.IntRect;
import dev.adventurecraft.awakening.util.DrawUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.Tesselator;
import org.lwjgl.input.Keyboard;

@Environment(EnvType.CLIENT)
public class AC_EditBox extends GuiComponent {

    private IntRect rect;
    private StringBuilder value;
    private int maxLength;
    private int tickCount;
    private boolean active = false;
    private boolean visible = true;

    private int activeTextColor;
    private int inactiveTextColor;
    private int boxBackColor;
    private int boxBorderColor;

    public AC_EditBox(IntRect rect, String value) {
        this.rect = rect;
        this.maxLength = Integer.MAX_VALUE;
        this.value = new StringBuilder(value);

        this.resetActiveTextColor();
        this.resetInactiveTextColor();
        this.resetBoxBackColor();
        this.resetBoxBorderColor();
    }

    public String getValue() {
        return this.value.toString();
    }

    public void setValue(String msg) {
        this.value.setLength(0);
        this.value.append(msg);
    }

    public void append(CharSequence value) {
        this.append(value, 0, value.length());
    }

    public void append(CharSequence value, int start, int end) {
        int len = Math.min(this.getFreeLength(), end - start);
        if (len > 0) {
            this.value.append(value, start, start + len);
        }
    }

    public void tick() {
        ++this.tickCount;
    }

    public void charTyped(char codePoint, int key) {
        if (!this.visible || !this.active) {
            return;
        }

        boolean ctrlDown = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
        boolean metaDown = Keyboard.isKeyDown(Keyboard.KEY_LMETA) || Keyboard.isKeyDown(Keyboard.KEY_RMETA);

        if (ctrlDown || metaDown) {
            if (key == Keyboard.KEY_V) {
                this.append(ClipboardHandler.getClipboard());
            }
            else if (key == Keyboard.KEY_C) {
                ClipboardHandler.setClipboard(this.value.toString());
            }
        }
        else if (key == 14 && !this.value.isEmpty()) {
            this.value.deleteCharAt(this.value.length() - 1);
        }
        else if (this.value.length() < this.maxLength && SharedConstants.acceptableLetters.indexOf(codePoint) >= 0) {
            this.value.append(codePoint);
        }
    }

    public void clicked(int mouseX, int mouseY, int button) {
        if (this.visible) {
            IntRect r = this.rect;
            boolean active = mouseX >= r.left() && mouseX < r.right() && mouseY >= r.top() && mouseY < r.bot();
            this.setActive(active);
        }
    }

    public void setActive(boolean active) {
        if (active && !this.active) {
            this.tickCount = 0;
        }
        this.active = active;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void render(Font font) {
        IntRect rect = this.rect;
        var ts = Tesselator.instance;

        DrawUtil.beginFill(ts);
        DrawUtil.fillRect(ts, rect.expand(new IntBorder(1)).asFloat(), this.boxBorderColor);
        DrawUtil.fillRect(ts, rect.asFloat(), this.boxBackColor);
        DrawUtil.endFill(ts);

        if (this.visible) {
            boolean showCaret = this.active && this.tickCount / 6 % 2 == 0;
            this.drawString(
                font,
                this.value + (showCaret ? "_" : ""),
                rect.x + 4,
                rect.y + (rect.h - 8) / 2,
                this.activeTextColor
            );
        }
        else {
            this.drawString(font, this.value.toString(), rect.x + 4, rect.y + (rect.h - 8) / 2, this.inactiveTextColor);
        }
    }

    public int getMaxLength() {
        return this.maxLength;
    }

    public void setMaxLength(int length) {
        this.maxLength = length;
    }

    public int getLength() {
        return this.value.length();
    }

    public int getFreeLength() {
        return this.getMaxLength() - this.getLength();
    }

    public IntRect getRect() {
        return this.rect;
    }

    public int getTickCount() {
        return this.tickCount;
    }

    public boolean isActive() {
        return this.active;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public int getActiveTextColor() {
        return activeTextColor;
    }

    public void setActiveTextColor(int color) {
        this.activeTextColor = color;
    }

    public void resetActiveTextColor() {
        this.setActiveTextColor(0xe0e0e0);
    }

    public int getInactiveTextColor() {
        return inactiveTextColor;
    }

    public void setInactiveTextColor(int color) {
        this.inactiveTextColor = color;
    }

    public void resetInactiveTextColor() {
        this.setInactiveTextColor(0x707070);
    }

    public int getBoxBackColor() {
        return this.boxBackColor;
    }

    public void setBoxBackColor(int value) {
        this.boxBackColor = value;
    }

    public void resetBoxBackColor() {
        this.setBoxBackColor(0xff000000);
    }

    public int getBoxBorderColor() {
        return this.boxBorderColor;
    }

    public void setBoxBorderColor(int value) {
        this.boxBorderColor = value;
    }

    public void resetBoxBorderColor() {
        this.setBoxBorderColor(0xffa0a0a0);
    }
}
