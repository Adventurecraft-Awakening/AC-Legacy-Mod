package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.MathF;
import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.Tessellator;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.List;

public abstract class ScrollableWidget extends GuiElement {

    public final Minecraft client;
    public final int width;
    public final int height;
    public int widgetX;
    public int widgetY;
    public final int contentTop;
    public final int contentBot;
    protected final int entryHeight;
    private final int scrollBackRight;
    private final int scrollBackLeft;

    private int field_1533;
    private int field_1534;
    private double dragDistance = -2.0;
    private double scrollAmount;
    private double lerpScrollY;
    private double targetScrollY;
    private double scrollY;
    private int prevEntryIndex = -1;
    private long prevClickTime = 0L;
    private boolean renderSelections = true;
    private int contentTopPadding;
    private boolean isUsingScrollbar;
    private boolean isScrolling;
    private boolean firstFrame = true;

    public ScrollableWidget(
        Minecraft minecraft,
        int x, int y, int width, int height,
        int contentTop, int contentBot, int entryHeight) {
        this.client = minecraft;
        this.width = width;
        this.height = height;
        this.widgetX = x;
        this.widgetY = y;
        this.contentTop = contentTop;
        this.contentBot = contentBot;
        this.entryHeight = entryHeight;
        this.scrollBackLeft = 0;
        this.scrollBackRight = width;
        this.contentTopPadding = 0;
    }

    protected abstract int getEntryCount();

    protected abstract void entryClicked(int entryIndex, boolean doubleClick);

    protected abstract boolean isEntrySelected(int entryIndex);

    protected int getTotalRenderHeight() {
        return this.getEntryCount() * this.entryHeight + this.contentTopPadding;
    }

    protected abstract void renderBackground();

    protected abstract void renderEntry(
        int entryIndex, double entryX, double entryY, int entryHeight, Tessellator tessellator);

    protected void mouseClicked(int entryX, int entryY) {
    }

    protected void beforeRender(double x, double y, Tessellator tessellator) {
    }

    protected void afterRender(int mouseX, int mouseY, float tickTime, Tessellator tessellator) {
    }

    public void setRenderSelections(boolean bl) {
        this.renderSelections = bl;
    }

    public void setContentTopPadding(int value) {
        this.contentTopPadding = value;
    }

    public int getEntryUnderPoint(int x, int y) {
        int center = this.width / 2 + this.widgetX;
        int left = center - 110;
        int right = center + 110;
        if (x >= left && x <= right) {
            int entryY = y - this.contentTop - this.contentTopPadding + (int) this.scrollY - 4 - this.widgetY;
            if (entryY >= 0) {
                int entryIndex = entryY / this.entryHeight;
                if (entryIndex >= 0 && entryIndex < this.getEntryCount()) {
                    return entryIndex;
                }
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
            double maxY = this.getTotalRenderHeight() - (this.contentBot - this.contentTop - 4);
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
        double maxY = totalHeight - (this.contentBot - this.contentTop - 4);
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

    public void buttonClicked(ButtonWidget button) {
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
        this.renderBackground();
        int entryCount = this.getEntryCount();
        int center = this.width / 2 + this.widgetX;
        int scrollBarLeft = center + 124;
        int scrollBarRight = scrollBarLeft + 6;

        int scrollBackLeft = this.scrollBackLeft + this.widgetX;
        int scrollBackRight = this.scrollBackRight + this.widgetX;
        int contentTop = this.contentTop + this.widgetY;
        int contentBot = this.contentBot + this.widgetY;
        int totalHeight = this.getTotalRenderHeight();

        if (this.firstFrame) {
            // Derived classes handle element count,
            // so we can only do initial layout before rendering.
            this.clampTargetScroll(totalHeight);
            this.lerpScrollY = this.targetScrollY;
            this.scrollY = this.targetScrollY;
            this.firstFrame = false;
        }

        if (Mouse.isButtonDown(0)) {
            if (this.dragDistance == -1.0) {
                boolean bl = true;
                if (mouseY >= contentTop && mouseY <= contentBot) {
                    int entryLeft = center - 110;
                    int entryRight = center + 110;
                    if (mouseX >= entryLeft && mouseX <= entryRight) {
                        int hoverY = mouseY - contentTop - this.contentTopPadding + (int) this.scrollY - 4;
                        int entryIndex = hoverY / this.entryHeight;
                        if (entryIndex >= 0 && hoverY >= 0 && entryIndex < entryCount) {
                            boolean doubleClick = entryIndex == this.prevEntryIndex && System.currentTimeMillis() - this.prevClickTime < 250L;
                            this.entryClicked(entryIndex, doubleClick);
                            this.prevEntryIndex = entryIndex;
                            this.prevClickTime = System.currentTimeMillis();
                        } else if (hoverY < 0) {
                            this.mouseClicked(mouseX - entryLeft, mouseY - contentTop + (int) this.scrollY - 4);
                            bl = false;
                        }
                    }

                    if (mouseX >= scrollBarLeft && mouseX <= scrollBarRight) {
                        this.scrollAmount = -1.0;
                        int n3 = totalHeight - (contentBot - contentTop - 4);
                        if (n3 < 1) {
                            n3 = 1;
                        }
                        int n2 = (int) Math.ceil((double) ((contentBot - contentTop) * (contentBot - contentTop)) / (double) totalHeight);
                        if (n2 < 32) {
                            n2 = 32;
                        }
                        if (n2 > contentBot - contentTop - 8) {
                            n2 = contentBot - contentTop - 8;
                        }
                        this.scrollAmount /= (double) (contentBot - contentTop - n2) / (double) n3;
                        this.isUsingScrollbar = true;
                    } else {
                        this.scrollAmount = 1.0;
                    }
                    this.dragDistance = bl ? (float) mouseY : -2.0;
                } else {
                    this.dragDistance = -2.0;
                }
            } else if (this.dragDistance >= 0.0) {
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
        this.scrollY = MathF.round(this.lerpScrollY, 3);
        if (!this.isScrolling) {
            this.clampTargetScroll(totalHeight);
        }

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_FOG);
        var ts = Tessellator.INSTANCE;
        this.renderContentBackground(scrollBackLeft, scrollBackRight, contentTop, contentBot, this.scrollY, ts);

        double entryBaseX = center - 92 - 16;
        double entryBaseY = contentTop + 4 - this.scrollY;
        this.beforeRender(entryBaseX, entryBaseY, ts);

        for (int entryIndex = 0; entryIndex < entryCount; ++entryIndex) {
            double entryY = entryBaseY + entryIndex * this.entryHeight + this.contentTopPadding;
            int entryContentHeight = this.entryHeight - 4;
            if (entryY > contentBot || entryY + entryContentHeight < contentTop) {
                continue;
            }

            if (this.renderSelections && this.isEntrySelected(entryIndex)) {
                int shadowStartX = center - 110;
                int shadowEndX = center + 110;
                GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                ts.start();
                ts.color(0x808080);
                ts.vertex(shadowStartX, entryY + entryContentHeight + 2, 0.0, 0.0, 1.0);
                ts.vertex(shadowEndX, entryY + entryContentHeight + 2, 0.0, 1.0, 1.0);
                ts.vertex(shadowEndX, entryY - 2, 0.0, 1.0, 0.0);
                ts.vertex(shadowStartX, entryY - 2, 0.0, 0.0, 0.0);

                ts.color(0);
                ts.vertex(shadowStartX + 1, entryY + entryContentHeight + 1, 0.0, 0.0, 1.0);
                ts.vertex(shadowEndX - 1, entryY + entryContentHeight + 1, 0.0, 1.0, 1.0);
                ts.vertex(shadowEndX - 1, entryY - 1, 0.0, 1.0, 0.0);
                ts.vertex(shadowStartX + 1, entryY - 1, 0.0, 0.0, 0.0);
                ts.tessellate();
                GL11.glEnable(GL11.GL_TEXTURE_2D);
            }
            this.renderEntry(entryIndex, entryBaseX, entryY, entryContentHeight, ts);
        }
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.client.textureManager.getTextureId("/gui/background.png"));
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        ts.start();
        this.renderBackground(this.widgetY, contentTop, 255, 255);
        this.renderBackground(contentBot, this.widgetY + this.height, 255, 255);
        ts.tessellate();
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        int n4 = 4;
        ts.start();
        ts.color(0, 0);
        ts.vertex(scrollBackLeft, contentTop + n4, 0.0, 0.0, 1.0);
        ts.vertex(scrollBackRight, contentTop + n4, 0.0, 1.0, 1.0);
        ts.color(0, 255);
        ts.vertex(scrollBackRight, contentTop, 0.0, 1.0, 0.0);
        ts.vertex(scrollBackLeft, contentTop, 0.0, 0.0, 0.0);

        ts.color(0, 255);
        ts.vertex(scrollBackLeft, contentBot, 0.0, 0.0, 1.0);
        ts.vertex(scrollBackRight, contentBot, 0.0, 1.0, 1.0);
        ts.color(0, 0);
        ts.vertex(scrollBackRight, contentBot - n4, 0.0, 1.0, 0.0);
        ts.vertex(scrollBackLeft, contentBot - n4, 0.0, 0.0, 0.0);
        ts.tessellate();

        int n3 = totalHeight - (contentBot - contentTop - 4);
        if (n3 > 0) {
            // Scrollbar rendering
            int n2 = (contentBot - contentTop) * (contentBot - contentTop) / totalHeight;
            if (n2 < 32) {
                n2 = 32;
            }
            if (n2 > contentBot - contentTop - 8) {
                n2 = contentBot - contentTop - 8;
            }
            int n = (int) this.clampTargetScroll(this.targetScrollY, totalHeight) * (contentBot - contentTop - n2) / n3 + contentTop;
            if (n < contentTop) {
                n = contentTop;
            }

            ts.start();
            ts.color(0, 255);
            ts.vertex(scrollBarLeft, contentBot, 0.0, 0.0, 1.0);
            ts.vertex(scrollBarRight, contentBot, 0.0, 1.0, 1.0);
            ts.vertex(scrollBarRight, contentTop, 0.0, 1.0, 0.0);
            ts.vertex(scrollBarLeft, contentTop, 0.0, 0.0, 0.0);

            ts.color(0x808080, 255);
            ts.vertex(scrollBarLeft, n + n2, 0.0, 0.0, 1.0);
            ts.vertex(scrollBarRight, n + n2, 0.0, 1.0, 1.0);
            ts.vertex(scrollBarRight, n, 0.0, 1.0, 0.0);
            ts.vertex(scrollBarLeft, n, 0.0, 0.0, 0.0);

            ts.color(0xC0C0C0, 255);
            ts.vertex(scrollBarLeft, n + n2 - 1, 0.0, 0.0, 1.0);
            ts.vertex(scrollBarRight - 1, n + n2 - 1, 0.0, 1.0, 1.0);
            ts.vertex(scrollBarRight - 1, n, 0.0, 1.0, 0.0);
            ts.vertex(scrollBarLeft, n, 0.0, 0.0, 0.0);
            ts.tessellate();
        }
        this.afterRender(mouseX, mouseY, tickTime, ts);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_BLEND);
    }

    private void renderBackground(int topY, int botY, int topAlpha, int botAlpha) {
        var ts = Tessellator.INSTANCE;
        float f = 32.0f;
        ts.color(0x404040, topAlpha);
        ts.vertex(this.widgetX + this.width, topY, 0.0, 0.0, (float) topY / f);
        ts.vertex(this.widgetX, topY, 0.0, (float) this.width / f, (float) topY / f);
        ts.color(0x404040, botAlpha);
        ts.vertex(this.widgetX, botY, 0.0, (float) this.width / f, (float) botY / f);
        ts.vertex(this.widgetX + this.width, botY, 0.0, 0.0, (float) botY / f);
    }

    protected void renderContentBackground(double left, double right, double top, double bot, double scroll, Tessellator ts) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.client.textureManager.getTextureId("/gui/background.png"));
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        double f2 = 32.0;
        ts.start();
        ts.color(0x202020);
        ts.vertex(left, bot, 0.0, left / f2, (bot + scroll) / f2);
        ts.vertex(right, bot, 0.0, right / f2, (bot + scroll) / f2);
        ts.vertex(right, top, 0.0, right / f2, (top + scroll) / f2);
        ts.vertex(left, top, 0.0, left / f2, (top + scroll) / f2);
        ts.tessellate();
    }
}