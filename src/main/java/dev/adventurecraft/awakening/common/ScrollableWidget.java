package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.layout.*;
import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import dev.adventurecraft.awakening.util.DrawUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.Tesselator;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.List;

import static dev.adventurecraft.awakening.util.DrawUtil.drawRect;
import static dev.adventurecraft.awakening.util.DrawUtil.fillRect;

public abstract class ScrollableWidget extends GuiComponent {

    public final Minecraft client;
    private IntRect layoutRect;
    private IntBorder layoutBorder = IntBorder.zero;
    private IntBorder layoutPadding = IntBorder.zero;

    protected final int entryHeight;

    private int scrollbarWidth;
    private int scrollUpButtonId;
    private int scrollDownButtonId;

    private double dragDistance = -2.0;
    private double scrollAmount;
    private Point lerpScroll = Point.zero;
    private Point targetScroll = Point.zero;
    private Point scroll = Point.zero;

    private int prevEntryIndex = -1;
    private long prevClickTime = 0L;
    private int edgeShadowHeight = 4;
    private boolean isUsingScrollbar;
    private boolean isScrolling;
    private boolean firstFrame = true;

    public ScrollableWidget(Minecraft minecraft, IntRect layoutRect, int entryHeight) {
        this.client = minecraft;
        this.layoutRect = layoutRect;

        this.entryHeight = entryHeight;
        this.scrollbarWidth = 6;
    }

    protected abstract int getEntryCount();

    protected void entryClicked(int entryIndex, int buttonIndex, boolean doubleClick) {
    }

    protected int getTotalContentHeight() {
        return this.getEntryCount() * this.entryHeight + this.layoutPadding.height();
    }

    protected abstract void renderEntry(Tesselator ts, int entryIndex, Point entryLocation, int entryHeight);

    protected boolean mouseClicked(IntPoint mouseLocation) {
        return false;
    }

    protected void beforeEntryRender(Tesselator ts, IntPoint mouseLocation, Point entryLocation) {
    }

    protected void afterRender(Tesselator ts, IntPoint mouseLocation, float tickTime) {
    }

    public void setEdgeShadowHeight(int value) {
        this.edgeShadowHeight = value;
    }

    public Point getLocationRelativeToContent(Point layoutLocation) {
        Point contentOrigin = this.getContentRect().location().asFloat();
        return layoutLocation.sub(contentOrigin).add(this.getScroll());
    }

    public int getEntryUnderPoint(Point location) {
        Point contentLocation = this.getLocationRelativeToContent(location);
        // TODO: also check contentLocation.x
        double entryIndex = contentLocation.y / this.entryHeight;
        if (entryIndex >= 0 && entryIndex < this.getEntryCount()) {
            return (int) entryIndex;
        }
        return -1;
    }

    public void registerButtons(List buttons, int upId, int downId) {
        this.scrollUpButtonId = upId;
        this.scrollDownButtonId = downId;
    }

    public void moveContent(double amount) {
        if (Math.abs(amount) == 0) {
            return;
        }

        if (!this.isUsingScrollbar && this.isScrolling) {
            // Drag with more and more friction
            double maxY = this.getTotalContentHeight() - this.layoutPadding.height();
            if (maxY < 0) {
                maxY /= 2;
            }
            if (this.targetScroll.y >= maxY) {
                double delta = this.targetScroll.y - maxY + amount;
                amount = amount / (1.0 + (delta * delta) / this.entryHeight);
            }
            else {
                double minY = Math.min(maxY, 0.0f);
                if (this.targetScroll.y < minY) {
                    double delta = minY - this.targetScroll.y - amount;
                    amount = amount / (1.0 + (delta * delta) / this.entryHeight);
                }
            }
        }
        this.targetScroll = this.targetScroll.add(0, amount);
    }

    private Point clampTargetScroll(Point scroll, double totalHeight) {
        double maxY = totalHeight - this.getBorderRect().height();
        if (maxY < 0) {
            maxY /= 2;
        }
        return scroll.clamp(Point.zero, new Point(0, maxY));
    }

    private void clampTargetScroll(double totalHeight) {
        this.targetScroll = this.clampTargetScroll(this.targetScroll, totalHeight);
    }

    public void buttonClicked(Button button) {
        if (!button.active) {
            return;
        }
        if (button.id == this.scrollUpButtonId) {
            this.moveContent(-(this.entryHeight * 2.0 / 3));
            this.dragDistance = -2.0;
        }
        else if (button.id == this.scrollDownButtonId) {
            this.moveContent(this.entryHeight * 2.0 / 3);
            this.dragDistance = -2.0;
        }
    }

    public void onMouseEvent() {
        int mouseScrollYDelta = Mouse.getEventDWheel();
        this.moveContent(-(double) mouseScrollYDelta * this.entryHeight);
    }

    public IntRect getBorderRect() {
        return this.layoutRect.shrink(this.layoutBorder);
    }

    public IntRect getContentRect() {
        return this.getBorderRect().shrink(this.layoutPadding);
    }

    public void render(IntPoint mousePoint, float tickTime) {
        IntRect borderRect = this.getBorderRect();
        IntRect contentRect = this.getContentRect();

        int entryCount = this.getEntryCount();
        int scrollBarLeft = this.layoutRect.right() - this.scrollbarWidth;
        int scrollBarRight = this.layoutRect.right();

        int contentTop = borderRect.top();
        int contentBot = borderRect.bot();
        int totalHeight = this.getTotalContentHeight();

        if (this.firstFrame) {
            // Derived classes handle element count,
            // so we can only do initial layout before rendering.
            this.clampTargetScroll(totalHeight);
            this.lerpScroll = this.targetScroll;
            this.scroll = this.targetScroll;
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
                if (mousePoint.y >= contentTop && mousePoint.y <= contentBot) {
                    boolean doDragging = buttonIndex == 0;

                    int entryIndex = this.getEntryUnderPoint(mousePoint.asFloat());
                    if (entryIndex != -1) {
                        boolean doubleClick = entryIndex == this.prevEntryIndex && System.currentTimeMillis() - this.prevClickTime < 250L;

                        this.entryClicked(entryIndex, buttonIndex, doubleClick);
                        this.prevEntryIndex = entryIndex;
                        this.prevClickTime = System.currentTimeMillis();
                    }
                    else {
                        if (this.mouseClicked(mousePoint)) {
                            doDragging = false;
                        }
                    }

                    if (this.scrollbarWidth > 0 && mousePoint.x >= scrollBarLeft && mousePoint.x <= scrollBarRight) {
                        this.scrollAmount = -1.0;
                        int n3 = totalHeight - (contentBot - contentTop);
                        if (n3 < 1) {
                            n3 = 1;
                        }
                        int n2 = (int) Math.ceil((double) ((contentBot - contentTop) * (contentBot - contentTop)) / (double) totalHeight);
                        if (n2 < 32) {
                            n2 = 32;
                        }
                        if (n2 > contentBot - contentTop) {
                            n2 = contentBot - contentTop;
                        }
                        this.scrollAmount /= (double) (contentBot - contentTop - n2) / (double) n3;
                        this.isUsingScrollbar = true;
                    }
                    else {
                        this.scrollAmount = 1.0;
                    }
                    this.dragDistance = doDragging ? (double) mousePoint.y : -2.0;
                }
                else {
                    this.dragDistance = -2.0;
                }
            }
            else if (buttonIndex == 0 && this.dragDistance >= 0.0) {
                this.moveContent(-((double) mousePoint.y - this.dragDistance) * this.scrollAmount);
                this.dragDistance = mousePoint.y;
                this.isScrolling = true;
            }
        }
        else {
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
        this.lerpScroll = this.lerpScroll.lerp(this.targetScroll, lerpFactor * deltaTime);
        this.scroll = this.lerpScroll.round();
        if (!this.isScrolling) {
            this.clampTargetScroll(totalHeight);
        }

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_FOG);
        var ts = Tesselator.instance;
        Rect contentBackRect = IntRect.fromEdges(this.layoutRect, borderRect).asFloat();
        this.renderContentBackground(ts, contentBackRect, this.scroll);

        Point entryLocation = contentRect.location().asFloat().sub(this.scroll);
        this.beforeEntryRender(ts, mousePoint, entryLocation);

        for (int entryIndex = 0; entryIndex < entryCount; ++entryIndex) {
            double entryY = entryLocation.y + entryIndex * this.entryHeight;
            if (entryY > contentBot || entryY + this.entryHeight < contentTop) {
                continue;
            }
            this.renderEntry(ts, entryIndex, new Point(entryLocation.x, entryY), this.entryHeight);
        }

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.client.textures.loadTexture("/gui/background.png"));
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        ts.begin();
        var bgColor = new IntCorner(0xff404040);
        this.renderBackground(ts, this.layoutRect.alongTop(this.layoutBorder.top).asFloat(), bgColor);
        this.renderBackground(ts, this.layoutRect.alongBot(this.layoutBorder.bot).asFloat(), bgColor);
        ts.end();
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        if (this.edgeShadowHeight > 0) {
            Rect topRect = contentBackRect.alongTop(this.edgeShadowHeight);
            Rect botRect = contentBackRect.alongBot(this.edgeShadowHeight);

            ts.begin();
            DrawUtil.fillRect(ts, topRect, IntCorner.vertical(0xff000000, 0), null);
            DrawUtil.fillRect(ts, botRect, IntCorner.vertical(0, 0xff000000), null);
            ts.end();
        }

        int n3 = totalHeight - (contentBot - contentTop);
        if (this.scrollbarWidth > 0 && n3 > 0) {
            // Scrollbar rendering
            double n2 = (contentBot - contentTop) * (contentBot - contentTop) / (double) totalHeight;
            if (n2 < 32) {
                n2 = 32;
            }
            if (n2 > contentBot - contentTop) {
                n2 = contentBot - contentTop;
            }
            Point scroll = this.clampTargetScroll(this.targetScroll, totalHeight);
            double n = scroll.y * (contentBot - contentTop - n2) / n3 + contentTop;
            if (n < contentTop) {
                n = contentTop;
            }

            Rect scrollBarBackRect = Rect.fromEdges(scrollBarLeft, contentTop, scrollBarRight, contentBot);
            Rect scrollHandleRect = Rect.fromEdges(scrollBarLeft, n, scrollBarRight, n + n2);
            ts.begin();
            fillRect(ts, scrollBarBackRect, new IntCorner(0x7f000000), null);
            DrawUtil.shadowRect(
                ts,
                scrollHandleRect,
                new Border(0, 1, 0, 1),
                new IntCorner(0x7f808080),
                new IntCorner(0x7fC0C0C0),
                null,
                null
            );
            ts.end();
        }

        this.afterRender(ts, mousePoint, tickTime);

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_BLEND);
    }

    protected void renderBackground(Tesselator ts, Rect rect, IntCorner color) {
        double uvSize = 32.0;
        Rect uv = rect.divide(new Rect(uvSize));
        fillRect(ts, rect, color, uv);
    }

    protected void renderContentBackground(Tesselator ts, Rect rect, Point scroll) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.client.textures.loadTexture("/gui/background.png"));
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        double uvSize = 32.0;
        Rect uv = rect.offset(scroll).divide(uvSize);
        ts.begin();
        fillRect(ts, rect, new IntCorner(0xff202020), uv);
        ts.end();
    }

    public void renderContentSelection(
        Tesselator ts,
        Rect rect,
        Border padding,
        IntCorner frontColor,
        IntCorner backColor,
        @Nullable Rect frontUv,
        @Nullable Rect backUv
    ) {
        DrawUtil.shadowRect(ts, rect, padding, frontColor, backColor, frontUv, backUv);
    }

    public IntRect getLayoutRect() {
        return this.layoutRect;
    }

    public IntBorder getLayoutPadding() {
        return this.layoutPadding;
    }

    public void setLayoutPadding(IntBorder value) {
        this.layoutPadding = value;
    }

    public IntBorder getLayoutBorder() {
        return this.layoutBorder;
    }

    public void setLayoutBorder(IntBorder value) {
        this.layoutBorder = value;
    }

    public Point getScroll() {
        return this.scroll;
    }

    public void setScroll(Point value, boolean lerp) {
        Point scroll = clampTargetScroll(value, getTotalContentHeight());
        this.targetScroll = scroll;
        if (!lerp) {
            this.scroll = scroll;
            this.lerpScroll = scroll;
        }
    }

    public double getScrollRow() {
        return this.targetScroll.y / this.entryHeight;
    }

    public void setScrollRow(double row, boolean lerp) {
        setScroll(new Point(this.scroll.x, row * this.entryHeight), lerp);
    }
}