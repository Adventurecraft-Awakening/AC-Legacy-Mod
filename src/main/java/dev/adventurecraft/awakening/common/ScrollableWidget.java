package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.util.MathF;
import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.Tesselator;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.List;

public abstract class ScrollableWidget extends GuiComponent {

    public final Minecraft client;
    private IntRect screenRect;
    private IntRect contentRect;

    protected final int entryHeight;
    private final int scrollBackRight;
    private final int scrollBackLeft;

    private int scrollbarX;
    private int scrollbarWidth;
    private int field_1533;
    private int field_1534;
    private double dragDistance = -2.0;
    private double scrollAmount;
    private double lerpScrollY;
    private double targetScrollY;
    private double scrollY;
    private int prevEntryIndex = -1;
    private long prevClickTime = 0L;
    private int contentTopPadding;
    private int contentBotPadding;
    private boolean renderEdgeShadows = true;
    private boolean isUsingScrollbar;
    private boolean isScrolling;
    private boolean firstFrame = true;

    public ScrollableWidget(
        Minecraft minecraft,
        IntRect screenRect,
        IntRect contentRect,
        int entryHeight) {
        this.client = minecraft;
        this.screenRect = screenRect;
        this.contentRect = contentRect;

        this.entryHeight = entryHeight;
        this.scrollBackLeft = 0;
        this.scrollBackRight = this.screenRect.w;

        this.scrollbarWidth = 6;
        this.scrollbarX = this.screenRect.w - this.scrollbarWidth;
        this.contentTopPadding = 0;
    }

    protected abstract int getEntryCount();

    protected void entryClicked(int entryIndex, int buttonIndex, boolean doubleClick) {
    }

    protected int getTotalRenderHeight() {
        return this.getEntryCount() * this.entryHeight + this.contentTopPadding + this.contentBotPadding;
    }

    protected abstract void renderEntry(
        int entryIndex, double entryX, double entryY, int entryHeight, Tesselator tessellator);

    protected boolean mouseClicked(int mouseX, int mouseY) {
        return false;
    }

    protected void beforeEntryRender(int mouseX, int mouseY, double entryX, double entryY, Tesselator tessellator) {
    }

    protected void afterRender(int mouseX, int mouseY, float tickTime, Tesselator tessellator) {
    }

    public void setContentTopPadding(int value) {
        this.contentTopPadding = value;
    }

    public void setContentBotPadding(int value) {
        this.contentBotPadding = value;
    }

    public void setRenderEdgeShadows(boolean value) {
        this.renderEdgeShadows = value;
    }

    public void setScrollbarSize(int x, int width) {
        this.scrollbarX = x;
        this.scrollbarWidth = width;
    }

    public int getEntryUnderPoint(int x, int y) {
        int entryY = y - this.contentRect.top() - this.contentTopPadding + (int) this.scrollY - this.screenRect.top();
        if (entryY >= 0) {
            int entryIndex = entryY / this.entryHeight;
            if (entryIndex >= 0 && entryIndex < this.getEntryCount()) {
                return entryIndex;
            }
        }
        return -1;
    }

    public void registerButtons(List list, int i, int j) {
        this.field_1533 = i;
        this.field_1534 = j;
    }

    public void moveContent(double amount) {
        if (Math.abs(amount) == 0) {
            return;
        }

        if (!this.isUsingScrollbar && this.isScrolling) {
            // Drag with more and more friction
            double maxY = this.getTotalRenderHeight() - this.contentRect.height();
            if (maxY < 0) {
                maxY /= 2;
            }
            if (this.targetScrollY >= maxY) {
                double delta = this.targetScrollY - maxY + amount;
                amount = amount / (1.0 + (delta * delta) / this.entryHeight);
            } else {
                double minY = Math.min(maxY, 0.0f);
                if (this.targetScrollY < minY) {
                    double delta = minY - this.targetScrollY - amount;
                    amount = amount / (1.0 + (delta * delta) / this.entryHeight);
                }
            }
        }
        this.targetScrollY += amount;
    }

    private double clampTargetScroll(double value, double totalHeight) {
        double maxY = totalHeight - this.contentRect.h;
        if (maxY < 0) {
            maxY /= 2;
        }
        if (value >= maxY) {
            value = maxY;
        } else {
            double minY = Math.min(maxY, 0.0f);
            if (value < minY) {
                value = minY;
            }
        }
        return value;
    }

    private void clampTargetScroll(double totalHeight) {
        this.targetScrollY = this.clampTargetScroll(this.targetScrollY, totalHeight);
    }

    public void buttonClicked(Button button) {
        if (!button.active) {
            return;
        }
        if (button.id == this.field_1533) {
            this.moveContent(-(this.entryHeight * 2.0 / 3));
            this.dragDistance = -2.0;
        } else if (button.id == this.field_1534) {
            this.moveContent(this.entryHeight * 2.0 / 3);
            this.dragDistance = -2.0;
        }
    }

    public void onMouseEvent() {
        int mouseScrollYDelta = Mouse.getEventDWheel();
        this.moveContent(-(double) mouseScrollYDelta * this.entryHeight);
    }

    public void render(int mouseX, int mouseY, float tickTime) {
        int entryCount = this.getEntryCount();
        int center = this.screenRect.w / 2 + this.screenRect.x;
        int scrollBarLeft = this.scrollbarX;
        int scrollBarRight = scrollBarLeft + this.scrollbarWidth;

        int scrollBackLeft = this.scrollBackLeft + this.screenRect.left();
        int scrollBackRight = this.scrollBackRight + this.screenRect.left();
        int contentTop = this.contentRect.top() + this.screenRect.top();
        int contentBot = this.contentRect.bottom() + this.screenRect.top();
        int totalHeight = this.getTotalRenderHeight();

        if (this.firstFrame) {
            // Derived classes handle element count,
            // so we can only do initial layout before rendering.
            this.clampTargetScroll(totalHeight);
            this.lerpScrollY = this.targetScrollY;
            this.scrollY = this.targetScrollY;
            this.firstFrame = false;
        }

        int buttonIndex = -1;
        for (int i = 0; i < Mouse.getButtonCount(); i++) {
            if (Mouse.isButtonDown(i)) {
                buttonIndex = i;
                break;
            }
        }

        if (buttonIndex != -1) {
            if (this.dragDistance == -1.0) {
                if (mouseY >= contentTop && mouseY <= contentBot) {
                    boolean doDragging = buttonIndex == 0;

                    int entryIndex = this.getEntryUnderPoint(mouseX, mouseY);
                    if (entryIndex != -1) {
                        boolean doubleClick = entryIndex == this.prevEntryIndex &&
                            System.currentTimeMillis() - this.prevClickTime < 250L;

                        this.entryClicked(entryIndex, buttonIndex, doubleClick);
                        this.prevEntryIndex = entryIndex;
                        this.prevClickTime = System.currentTimeMillis();
                    } else {
                        if (this.mouseClicked(mouseX, mouseY)) {
                            doDragging = false;
                        }
                    }

                    if (this.scrollbarWidth > 0 && mouseX >= scrollBarLeft && mouseX <= scrollBarRight) {
                        this.scrollAmount = -1.0;
                        int n3 = totalHeight - (contentBot - contentTop);
                        if (n3 < 1) {
                            n3 = 1;
                        }
                        int n2 = (int) Math.ceil(
                            (double) ((contentBot - contentTop) * (contentBot - contentTop)) / (double) totalHeight);
                        if (n2 < 32) {
                            n2 = 32;
                        }
                        if (n2 > contentBot - contentTop) {
                            n2 = contentBot - contentTop;
                        }
                        this.scrollAmount /= (double) (contentBot - contentTop - n2) / (double) n3;
                        this.isUsingScrollbar = true;
                    } else {
                        this.scrollAmount = 1.0;
                    }
                    this.dragDistance = doDragging ? (double) mouseY : -2.0;
                } else {
                    this.dragDistance = -2.0;
                }
            } else if (buttonIndex == 0 && this.dragDistance >= 0.0) {
                this.moveContent(-((double) mouseY - this.dragDistance) * this.scrollAmount);
                this.dragDistance = mouseY;
                this.isScrolling = true;
            }
        } else {
            this.dragDistance = -1.0;
            this.isUsingScrollbar = false;
            this.isScrolling = false;
        }

        if (this.isUsingScrollbar) {
            // Prevent bouncing
            this.clampTargetScroll(totalHeight);
        }
        double lerpFactor = this.isUsingScrollbar ? 45 : 25;
        double deltaTime = ((ExMinecraft) this.client).getFrameTime();
        this.lerpScrollY = MathF.lerp(lerpFactor * deltaTime, this.lerpScrollY, this.targetScrollY);
        this.scrollY = Math.round(this.lerpScrollY);
        if (!this.isScrolling) {
            this.clampTargetScroll(totalHeight);
        }

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_FOG);
        var ts = Tesselator.instance;
        this.renderContentBackground(scrollBackLeft, scrollBackRight, contentTop, contentBot, this.scrollY, ts);

        double entryBaseX = center;
        double entryBaseY = contentTop - this.scrollY;
        this.beforeEntryRender(mouseX, mouseY, entryBaseX, entryBaseY, ts);

        for (int entryIndex = 0; entryIndex < entryCount; ++entryIndex) {
            double entryY = entryBaseY + entryIndex * this.entryHeight + this.contentTopPadding;
            if (entryY > contentBot || entryY + this.entryHeight < contentTop) {
                continue;
            }
            this.renderEntry(entryIndex, entryBaseX, entryY, this.entryHeight, ts);
        }
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.client.textures.loadTexture("/gui/background.png"));
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        ts.begin();
        this.renderBackground(this.screenRect.top(), contentTop, 255, 255);
        this.renderBackground(contentBot, this.screenRect.bottom(), 255, 255);
        ts.end();
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        if (renderEdgeShadows) {
            int n4 = 4;
            ts.begin();
            ts.color(0, 0);
            ts.vertexUV(scrollBackLeft, contentTop + n4, 0.0, 0.0, 1.0);
            ts.vertexUV(scrollBackRight, contentTop + n4, 0.0, 1.0, 1.0);
            ts.color(0, 255);
            ts.vertexUV(scrollBackRight, contentTop, 0.0, 1.0, 0.0);
            ts.vertexUV(scrollBackLeft, contentTop, 0.0, 0.0, 0.0);

            ts.color(0, 255);
            ts.vertexUV(scrollBackLeft, contentBot, 0.0, 0.0, 1.0);
            ts.vertexUV(scrollBackRight, contentBot, 0.0, 1.0, 1.0);
            ts.color(0, 0);
            ts.vertexUV(scrollBackRight, contentBot - n4, 0.0, 1.0, 0.0);
            ts.vertexUV(scrollBackLeft, contentBot - n4, 0.0, 0.0, 0.0);
            ts.end();
        }

        int n3 = totalHeight - (contentBot - contentTop);
        if (this.scrollbarWidth > 0 && n3 > 0) {
            // Scrollbar rendering
            int n2 = (contentBot - contentTop) * (contentBot - contentTop) / totalHeight;
            if (n2 < 32) {
                n2 = 32;
            }
            if (n2 > contentBot - contentTop) {
                n2 = contentBot - contentTop;
            }
            int n = (int) this.clampTargetScroll(
                this.targetScrollY, totalHeight) * (contentBot - contentTop - n2) / n3 + contentTop;
            if (n < contentTop) {
                n = contentTop;
            }

            ts.begin();
            ts.color(0, 127);
            ts.vertexUV(scrollBarLeft, contentBot, 0.0, 0.0, 1.0);
            ts.vertexUV(scrollBarRight, contentBot, 0.0, 1.0, 1.0);
            ts.vertexUV(scrollBarRight, contentTop, 0.0, 1.0, 0.0);
            ts.vertexUV(scrollBarLeft, contentTop, 0.0, 0.0, 0.0);

            ts.color(0x808080, 127);
            ts.vertexUV(scrollBarLeft, n + n2, 0.0, 0.0, 1.0);
            ts.vertexUV(scrollBarRight, n + n2, 0.0, 1.0, 1.0);
            ts.vertexUV(scrollBarRight, n, 0.0, 1.0, 0.0);
            ts.vertexUV(scrollBarLeft, n, 0.0, 0.0, 0.0);

            ts.color(0xC0C0C0, 127);
            ts.vertexUV(scrollBarLeft, n + n2 - 1, 0.0, 0.0, 1.0);
            ts.vertexUV(scrollBarRight - 1, n + n2 - 1, 0.0, 1.0, 1.0);
            ts.vertexUV(scrollBarRight - 1, n, 0.0, 1.0, 0.0);
            ts.vertexUV(scrollBarLeft, n, 0.0, 0.0, 0.0);
            ts.end();
        }
        this.afterRender(mouseX, mouseY, tickTime, ts);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_BLEND);
    }

    protected void renderBackground(int topY, int botY, int topAlpha, int botAlpha) {
        var ts = Tesselator.instance;
        float f = 32.0f;
        int width = this.screenRect.width();
        ts.color(0x404040, topAlpha);
        ts.vertexUV(this.screenRect.right(), topY, 0.0, 0.0, topY / f);
        ts.vertexUV(this.screenRect.left(), topY, 0.0, width / f, topY / f);
        ts.color(0x404040, botAlpha);
        ts.vertexUV(this.screenRect.left(), botY, 0.0, width / f, botY / f);
        ts.vertexUV(this.screenRect.right(), botY, 0.0, 0.0, botY / f);
    }

    protected void renderContentBackground(
        double left, double right, double top, double bot, double scroll, Tesselator ts) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.client.textures.loadTexture("/gui/background.png"));
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        double f2 = 32.0;
        ts.begin();
        ts.color(0x202020);
        ts.vertexUV(left, bot, 0.0, left / f2, (bot + scroll) / f2);
        ts.vertexUV(right, bot, 0.0, right / f2, (bot + scroll) / f2);
        ts.vertexUV(right, top, 0.0, right / f2, (top + scroll) / f2);
        ts.vertexUV(left, top, 0.0, left / f2, (top + scroll) / f2);
        ts.end();
    }

    public void renderContentSelection(
        double x, double y, double width, double height,
        double borderSize, int borderColor, int backColor, Tesselator ts) {

        double left = x;
        double right = x + width;

        ts.color(borderColor, (int) ((Integer.toUnsignedLong(borderColor) & 0xff000000L) >> 24));
        ts.vertexUV(left, y + height, 0.0, 0.0, 1.0);
        ts.vertexUV(right, y + height, 0.0, 1.0, 1.0);
        ts.vertexUV(right, y, 0.0, 1.0, 0.0);
        ts.vertexUV(left, y, 0.0, 0.0, 0.0);

        ts.color(backColor, (int) ((Integer.toUnsignedLong(backColor) & 0xff000000L) >> 24));
        ts.vertexUV(left + borderSize, y + height - borderSize, 0.0, 0.0, 1.0);
        ts.vertexUV(right - borderSize, y + height - borderSize, 0.0, 1.0, 1.0);
        ts.vertexUV(right - borderSize, y + borderSize, 0.0, 1.0, 0.0);
        ts.vertexUV(left + borderSize, y + borderSize, 0.0, 0.0, 0.0);
    }

    public IntRect getContentRect() {
        return contentRect;
    }

    public IntRect getScreenRect() {
        return screenRect;
    }

    public int getContentTop() {
        return this.contentRect.top();
    }

    public int getContentBot() {
        return this.contentRect.bottom();
    }

    public int getContentTopPadding() {
        return this.contentTopPadding;
    }

    public int getContentBotPadding() {
        return this.contentBotPadding;
    }

    public double getScrollY() {
        return this.scrollY;
    }

    public void setScrollY(double value, boolean lerp) {
        value = clampTargetScroll(value, getTotalRenderHeight());
        this.targetScrollY = value;
        if (!lerp) {
            this.scrollY = value;
            this.lerpScrollY = value;
        }
    }

    public double getScrollRow() {
        return this.targetScrollY / this.entryHeight;
    }

    public void setScrollRow(double row, boolean lerp) {
        setScrollY(row * this.entryHeight, lerp);
    }
}