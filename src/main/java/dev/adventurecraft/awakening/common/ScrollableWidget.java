package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.client.render.compat.LegacyPassBridge;
import dev.adventurecraft.awakening.client.render.UiDrawContext;
import dev.adventurecraft.awakening.layout.*;
import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import dev.adventurecraft.awakening.util.DrawUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.Tesselator;
import org.lwjgl.input.Mouse;

import javax.annotation.Nullable;
import java.util.List;

import static dev.adventurecraft.awakening.util.DrawUtil.fillRect;

public abstract class ScrollableWidget extends GuiComponent {

    public static final long DOUBLE_CLICK_TIME = 250L;

    public final Minecraft client;
    private IntRect layoutRect;
    private IntBorder layoutBorder;
    private IntBorder layoutPadding;

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

        this.layoutBorder = IntBorder.zero;
        this.layoutPadding = new IntBorder(0, this.edgeShadowHeight);

        this.entryHeight = entryHeight;
        this.scrollbarWidth = 6;
    }

    protected abstract int getEntryCount();

    protected void entryClicked(int entryIndex, int buttonIndex, boolean doubleClick) {
    }

    protected int getTotalContentHeight() {
        return this.getEntryCount() * this.entryHeight + this.layoutPadding.height();
    }

    protected void renderEntry(Tesselator ts, int entryIndex, Point entryLocation, int entryHeight) { }

    protected void renderEntry(UiDrawContext ui, int index, Point location, int height) {
        LegacyPassBridge.legacyUi(ui, () -> this.renderEntry(Tesselator.instance, index, location, height), false);
    }

    protected void beforeEntryRender(UiDrawContext ui, IntPoint mouse, Point location) {
        LegacyPassBridge.legacyUi(ui, () -> this.beforeEntryRender(Tesselator.instance, mouse, location), false);
    }

    protected void afterRender(UiDrawContext ui, IntPoint mouse, float time) {
        LegacyPassBridge.legacyUi(ui, () -> this.afterRender(Tesselator.instance, mouse, time), false);
    }

    protected boolean mouseClicked(IntPoint mouseLocation, int buttonIndex, boolean doubleClick) {
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
        if (contentLocation.x >= 0 && contentLocation.x < this.getContentRect().width()) {
            double entryIndex = contentLocation.y / this.entryHeight;
            if (entryIndex >= 0 && entryIndex < this.getEntryCount()) {
                return (int) entryIndex;
            }
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

    public boolean buttonClicked(Button button) {
        if (!button.active) {
            return false;
        }

        if (button.id == this.scrollUpButtonId) {
            this.moveContent(-(this.entryHeight * 2.0 / 3));
            this.dragDistance = -2.0;
            return true;
        }
        else if (button.id == this.scrollDownButtonId) {
            this.moveContent(this.entryHeight * 2.0 / 3);
            this.dragDistance = -2.0;
            return true;
        }
        return false;
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
        try (var ui = LegacyPassBridge.uiContext(this.client.font, this.client.textures)) {
            this.render(mousePoint, tickTime, ui);
        }
    }

    public void render(IntPoint mousePoint, float tickTime, UiDrawContext ui) {
        IntRect borderRect = this.getBorderRect();

        var scrollBarBorder = new IntBorder(
            this.layoutRect.right() - this.scrollbarWidth,
            this.layoutRect.right(),
            0,
            0
        );

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
                if (borderRect.containsY(mousePoint.y)) {
                    boolean doDragging = buttonIndex == 0;

                    long currentMillis = System.currentTimeMillis();
                    boolean isDoubleClick = currentMillis - this.prevClickTime < DOUBLE_CLICK_TIME;
                    this.prevClickTime = currentMillis;

                    int entryIndex = this.getEntryUnderPoint(mousePoint.asFloat());
                    if (entryIndex != -1) {
                        boolean isEntryDoubleClick = entryIndex == this.prevEntryIndex && isDoubleClick;
                        this.entryClicked(entryIndex, buttonIndex, isEntryDoubleClick);
                        this.prevEntryIndex = entryIndex;
                    }
                    else if (this.mouseClicked(mousePoint, buttonIndex, isDoubleClick)) {
                        doDragging = false;
                    }

                    if (scrollBarBorder.width() > 0 && scrollBarBorder.containsX(mousePoint.x)) {
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

        this.doRender(ui, mousePoint, tickTime, borderRect, totalHeight, scrollBarBorder);
    }

    private void doRender(UiDrawContext ui, IntPoint mousePoint, float tickTime, IntRect borderRect,
                          int totalHeight, IntBorder scrollBarBorder) {
        int contentTop = borderRect.top(), contentBot = borderRect.bot();
        IntRect contentRect = this.getContentRect();
        Rect contentBackRect = IntRect.fromEdges(this.layoutRect, borderRect).asFloat();
        this.renderContentBackground(ui, contentBackRect, this.scroll);
        Point entryLocation = contentRect.location().asFloat().sub(this.scroll);
        this.beforeEntryRender(ui, mousePoint, entryLocation);
        for (int index = 0; index < this.getEntryCount(); index++) {
            double entryY = entryLocation.y + index * this.entryHeight;
            if (entryY > contentBot || entryY + this.entryHeight < contentTop) continue;
            this.renderEntry(ui, index, new Point(entryLocation.x, entryY), this.entryHeight);
        }
        var backgroundColor = new IntCorner(0xff404040);
        this.renderBackground(ui, this.layoutRect.alongTop(this.layoutBorder.top).asFloat(), backgroundColor);
        this.renderBackground(ui, this.layoutRect.alongBot(this.layoutBorder.bot).asFloat(), backgroundColor);
        if (this.edgeShadowHeight > 0) {
            ui.fill(contentBackRect.alongTop(this.edgeShadowHeight), IntCorner.vertical(0xff000000, 0));
            ui.fill(contentBackRect.alongBot(this.edgeShadowHeight), IntCorner.vertical(0, 0xff000000));
        }
        int overflow = totalHeight - (contentBot - contentTop);
        if (this.scrollbarWidth > 0 && overflow > 0) {
            double handleHeight = Math.clamp((contentBot - contentTop) * (contentBot - contentTop) / (double) totalHeight,
                Math.min(32, contentBot - contentTop), contentBot - contentTop);
            Point scroll = this.clampTargetScroll(this.targetScroll, totalHeight);
            double y = Math.max(contentTop, scroll.y * (contentBot - contentTop - handleHeight) / overflow + contentTop);
            ui.fill(Rect.fromEdges(scrollBarBorder.left, contentTop, scrollBarBorder.right, contentBot), 0x7f000000);
            this.renderContentSelection(ui, Rect.fromEdges(scrollBarBorder.left, y, scrollBarBorder.right, y + handleHeight),
                new Border(0, 1, 0, 1), new IntCorner(0x7f808080), new IntCorner(0x7fc0c0c0), null, null);
        }
        this.afterRender(ui, mousePoint, tickTime);
    }

    protected void renderBackground(UiDrawContext ui, Rect rect, IntCorner color) {
        ui.image(ui.texture("/gui/background.png"), rect, rect.divide(32), color);
    }

    protected void renderContentBackground(UiDrawContext ui, Rect rect, Point scroll) {
        ui.image(ui.texture("/gui/background.png"), rect, rect.offset(scroll).divide(32), new IntCorner(0xff202020));
    }

    public void renderContentSelection(UiDrawContext ui, Rect rect, Border padding, IntCorner frontColor,
                                       IntCorner backColor, @Nullable Rect frontUv, @Nullable Rect backUv) {
        Rect front = rect.shrink(padding.min(Border.zero));
        Rect back = rect.shrink(padding.max(Border.zero));
        if (frontUv == null) ui.fill(front, frontColor);
        else ui.image(ui.texture("/gui/background.png"), front, frontUv, frontColor);
        if (backUv == null) ui.fill(back, backColor);
        else ui.image(ui.texture("/gui/background.png"), back, backUv, backColor);
    }

    /** Compatibility callbacks for external widget subclasses. */
    protected void renderBackground(Tesselator ts, Rect rect, IntCorner color) {
        fillRect(ts, rect, color, rect.divide(32));
    }

    protected void renderContentBackground(Tesselator ts, Rect rect, Point scroll) { }

    public void renderContentSelection(Tesselator ts, Rect rect, Border padding, IntCorner frontColor,
                                       IntCorner backColor, @Nullable Rect frontUv, @Nullable Rect backUv) {
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
