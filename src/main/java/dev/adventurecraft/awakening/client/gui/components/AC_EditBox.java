package dev.adventurecraft.awakening.client.gui.components;

import dev.adventurecraft.awakening.client.gui.EditSelection;
import dev.adventurecraft.awakening.common.ClipboardHandler;
import dev.adventurecraft.awakening.extension.client.render.ExTextRenderer;
import dev.adventurecraft.awakening.layout.IntBorder;
import dev.adventurecraft.awakening.layout.IntRect;
import dev.adventurecraft.awakening.util.DrawUtil;
import dev.adventurecraft.awakening.util.MathF;
import dev.adventurecraft.awakening.util.ReverseMatcher;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.Tesselator;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;

import java.util.regex.Pattern;

@Environment(EnvType.CLIENT)
public class AC_EditBox extends GuiComponent {

    private static final Pattern BOUNDARY_PATTERN = Pattern.compile("\\w\\W", Pattern.MULTILINE);

    private IntRect rect;
    private StringBuilder value;
    private int maxLength;
    private @Nullable EditSelection selection;

    private int tickCount;
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
            this.value.insert(this.turnSelectionIntoInsert(len), value, start, start + len);
        }
    }

    public void append(char value) {
        if (this.value.length() < this.maxLength) {
            this.value.insert(this.turnSelectionIntoInsert(1), value);
        }
    }

    private int turnSelectionIntoInsert(int length) {
        EditSelection sel = this.selection;
        if (sel == null) {
            return this.value.length();
        }

        int start = sel.absStart();
        if (!sel.isEmpty()) {
            this.value.delete(start, sel.absEnd());
        }

        this.selection = new EditSelection(start + length);
        return start;
    }

    public void deleteSelection() {
        EditSelection sel = this.selection;
        if (sel == null || sel.isEmpty()) {
            return;
        }

        int start = sel.absStart();
        this.value.delete(start, sel.absEnd());
        this.selection = new EditSelection(start);
    }

    public void tick() {
        ++this.tickCount;
    }

    public void charTyped(char codePoint, int key) {
        if (!this.isVisible() || !this.isActive()) {
            return;
        }

        boolean ctrlDown = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
        boolean metaDown = Keyboard.isKeyDown(Keyboard.KEY_LMETA) || Keyboard.isKeyDown(Keyboard.KEY_RMETA);
        boolean shiftDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);

        EditSelection sel = this.selection;
        if (sel != null && (key == Keyboard.KEY_LEFT || key == Keyboard.KEY_RIGHT)) {
            int dir = (key == Keyboard.KEY_LEFT)
                ? this.getBackMoveAmount(sel, ctrlDown)
                : this.getForwardMoveAmount(sel, ctrlDown);

            if (shiftDown) {
                this.expandSelection(sel, dir);
            }
            else {
                this.moveSelection(sel, dir);
            }
        }
        else if (sel != null && key == Keyboard.KEY_DELETE) {
            this.expandSelection(sel, this.getForwardMoveAmount(sel, ctrlDown));
            this.deleteSelection();
        }
        else if (sel != null && key == Keyboard.KEY_BACK) {
            this.expandSelection(sel, this.getBackMoveAmount(sel, ctrlDown));
            this.deleteSelection();
        }
        else if (ctrlDown || metaDown) {
            if (key == Keyboard.KEY_V) {
                this.append(ClipboardHandler.getClipboard());
            }
            else if (key == Keyboard.KEY_C) {
                ClipboardHandler.setClipboard(this.value.toString());
            }
            else if (key == Keyboard.KEY_X) {
                if (sel != null && !sel.isEmpty()) {
                    ClipboardHandler.setClipboard(this.value.substring(sel.absStart(), sel.absEnd()));
                    this.deleteSelection();
                }
            }
            else if (key == Keyboard.KEY_A) {
                this.selection = new EditSelection(0, this.value.length());
            }
        }
        else if (SharedConstants.acceptableLetters.indexOf(codePoint) >= 0) {
            this.append(codePoint);
        }
    }

    private int getForwardMoveAmount(EditSelection origin, boolean jump) {
        if (!jump) {
            return 1;
        }
        var matcher = BOUNDARY_PATTERN.matcher(this.value);
        int start = Math.min(origin.end(), matcher.regionEnd());
        if (matcher.find(start)) {
            return matcher.end() - start - 1;
        }
        return matcher.regionEnd() - start;
    }

    private int getBackMoveAmount(EditSelection origin, boolean jump) {
        if (!jump) {
            return -1;
        }
        int start = Math.max(0, origin.end());
        var matcher = new ReverseMatcher(BOUNDARY_PATTERN.matcher(this.value));
        if (matcher.find(start)) {
            return matcher.start() - start + 1;
        }
        return -start;
    }

    public void expandSelection(EditSelection origin, int amount) {
        int end = MathF.clamp(origin.end() + amount, 0, this.value.length());
        this.selection = new EditSelection(origin.start(), end);
    }

    public void moveSelection(EditSelection origin, int amount) {
        int newStart = amount < 0 ? origin.absStart() : origin.absEnd();
        int newAmount = origin.isEmpty() ? amount : 0;
        int start = Math.clamp(newStart + newAmount, 0, this.value.length());
        this.selection = new EditSelection(start);
    }

    public void clicked(int mouseX, int mouseY, int button) {
        if (this.visible) {
            IntRect r = this.rect;
            boolean active = mouseX >= r.left() && mouseX < r.right() && mouseY >= r.top() && mouseY < r.bot();
            this.setActive(active);
        }
    }

    public void setActive(boolean active) {
        if (active) {
            if (!this.isActive()) {
                this.tickCount = 0;
                this.selection = new EditSelection(this.value.length());
            }
        }
        else {
            this.selection = null;
        }
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void render(Font font) {
        var ts = Tesselator.instance;
        var state = ((ExTextRenderer) font).createState();

        EditSelection sel = this.selection;
        IntRect rect = this.rect;
        int x = rect.x + 4;
        int y = rect.y + (rect.h - 8) / 2;

        DrawUtil.beginFill(ts);
        DrawUtil.fillRect(ts, rect.expand(new IntBorder(1)).asFloat(), this.boxBorderColor);
        DrawUtil.fillRect(ts, rect.asFloat(), this.boxBackColor);

        if (sel != null && !sel.isEmpty()) {
            int i = sel.absStart();
            int left = x - 1 + state.measureText(this.value, 0, i).width();
            int right = left + 1 + state.measureText(this.value, i, sel.absEnd()).width();
            var selRect = IntRect.fromEdges(left, rect.top(), right, rect.bot());

            DrawUtil.fillRect(ts, selRect.asFloat(), 0xa00000ff);
        }
        DrawUtil.endFill(ts);

        state.setColor(this.visible ? this.activeTextColor : this.inactiveTextColor);
        state.setShadowToColor();

        state.begin(ts);
        state.drawText(this.value, x, y);
        if (sel != null) {
            boolean showCaret = this.tickCount / 6 % 2 == 0;
            if (showCaret) {
                int width = state.measureText(this.value, 0, sel.end()).width();
                state.drawText("_", x + width, y + 1);
            }
        }
        state.end();
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
        return this.selection != null;
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
        this.setActiveTextColor(0xffe0e0e0);
    }

    public int getInactiveTextColor() {
        return inactiveTextColor;
    }

    public void setInactiveTextColor(int color) {
        this.inactiveTextColor = color;
    }

    public void resetInactiveTextColor() {
        this.setInactiveTextColor(0xff707070);
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
