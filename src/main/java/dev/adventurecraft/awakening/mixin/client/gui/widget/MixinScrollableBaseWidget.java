package dev.adventurecraft.awakening.mixin.client.gui.widget;

import dev.adventurecraft.awakening.common.ScrollableWidget;
import dev.adventurecraft.awakening.extension.client.gui.widget.ExScrollableBaseWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ScrollableBaseWidget;
import net.minecraft.client.render.Tessellator;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ScrollableBaseWidget.class)
public abstract class MixinScrollableBaseWidget implements ExScrollableBaseWidget {

    @Unique
    private ScrollableWidget rootWidget;

    @Unique
    private boolean doRenderStatItemSlot;

    @Unique
    private boolean renderSelections = true;

    @Unique
    private int hoveredEntry = -1;

    @Shadow
    protected abstract void method_1255(int i, int j);

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(
        Minecraft instance, int width, int height,
        int contentTop, int contentBot, int entryHeight, CallbackInfo ci) {

        MixinScrollableBaseWidget self = this;
        this.rootWidget = new ScrollableWidget(
            instance, 0, 0, width, height, contentTop, contentBot, entryHeight) {
            @Override
            protected int getEntryCount() {
                return self.getSize();
            }

            @Override
            protected void entryClicked(int entryIndex, boolean doubleClick) {
                self.entryClicked(entryIndex, doubleClick);
            }

            @Override
            public int getEntryUnderPoint(int x, int y) {
                int center = this.width / 2 + this.widgetX;
                int left = center - 110;
                int right = center + 110;
                if (x >= left && x <= right) {
                    int entryY = y - this.contentTop - this.getContentTopPadding() + (int) this.getScrollY() - 4 - this.widgetY;
                    if (entryY >= 0) {
                        int entryIndex = entryY / this.entryHeight;
                        if (entryIndex >= 0 && entryIndex < this.getEntryCount()) {
                            return entryIndex;
                        }
                    }
                }
                return -1;
            }

            @Override
            protected boolean mouseClicked(int mouseX, int mouseY) {
                int contentTop = this.contentTop + this.widgetY;
                int scrollY = (int) this.getScrollY();
                int entryLeft = mouseX - (this.width / 2 + this.widgetX - 110);
                int entryTop = mouseY - contentTop + scrollY - 4;
                int hoverY = mouseY - contentTop - this.getContentTopPadding() + scrollY - 4;
                if (hoverY <= 0) {
                    self.mouseClicked(entryLeft, entryTop);
                }
                return false;
            }

            @Override
            protected int getTotalRenderHeight() {
                return self.getTotalRenderHeight() + 4;
            }

            @Override
            protected void renderContentBackground(
                double left, double right, double top, double bot, double scroll, Tessellator ts) {
                self.renderBackground();
                super.renderContentBackground(left, right, top, bot, scroll, ts);
            }

            @Override
            protected void renderEntry(int entryIndex, double entryX, double entryY, int entryHeight, Tessellator ts) {
                int x = (int) Math.floor(entryX) - 92 - 16;
                int y = (int) Math.floor(entryY) + 4;

                if (self.renderSelections) {
                    boolean selected = self.isWorldSelected(entryIndex);
                    if (selected || hoveredEntry == entryIndex) {
                        boolean isHover = !selected && self.hoveredEntry == entryIndex;
                        int borderColor = isHover ? 0x80808080 : 0xff808080;
                        int backColor = isHover ? 0x80000000 : 0xff000000;

                        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
                        GL11.glDisable(GL11.GL_TEXTURE_2D);
                        GL11.glEnable(GL11.GL_BLEND);
                        ts.start();
                        self.rootWidget.renderContentSelection(
                            x - 2, y - 2, 220, entryHeight, 1, borderColor, backColor, ts);
                        ts.tessellate();
                        GL11.glEnable(GL11.GL_TEXTURE_2D);
                        GL11.glDisable(GL11.GL_BLEND);
                    }
                }
                self.renderStatEntry(entryIndex, x, y, entryHeight - 4, ts);
            }

            @Override
            protected void beforeEntryRender(int mouseX, int mouseY, double entryX, double entryY, Tessellator ts) {
                if (self.doRenderStatItemSlot) {
                    int sX = (int) Math.floor(entryX) - 92 - 16;
                    int sY = (int) Math.floor(entryY) + 4;
                    self.renderStatItemSlot(sX, sY, ts);
                }

                self.hoveredEntry = this.getEntryUnderPoint(mouseX, mouseY);
            }

            @Override
            protected void afterRender(int mouseX, int mouseY, float tickTime, Tessellator ts) {
                self.method_1255(mouseX, mouseY);
            }
        };
    }

    @Shadow
    protected abstract int getSize();

    @Shadow
    protected abstract void entryClicked(int entryIndex, boolean doubleClick);

    @Shadow
    protected abstract boolean isWorldSelected(int entryIndex);

    @Shadow
    protected abstract int getTotalRenderHeight();

    @Shadow
    protected abstract void mouseClicked(int x, int y);

    @Shadow
    protected abstract void renderBackground();

    @Shadow
    protected abstract void renderStatEntry(int entryIndex, int x, int y, int height, Tessellator ts);

    @Shadow
    protected abstract void renderStatItemSlot(int x, int y, Tessellator ts);

    @Overwrite
    public void method_1260(boolean bl) {
        this.renderSelections = bl;
    }

    @Inject(method = "method_1261", at = @At("HEAD"))
    public void capture_method_1261(boolean bl, int i, CallbackInfo ci) {
        this.doRenderStatItemSlot = bl;
        int contentYOffset = bl ? i : 0;
        this.rootWidget.setContentTopPadding(contentYOffset);
    }

    @Overwrite
    public int method_1262(int x, int y) {
        return this.rootWidget.getEntryUnderPoint(x, y);
    }

    @Overwrite
    public void registerButtons(List list, int i, int j) {
        this.rootWidget.registerButtons(list, i, j);
    }

    @Overwrite
    public void buttonClicked(ButtonWidget arg) {
        this.rootWidget.buttonClicked(arg);
    }

    @Overwrite
    public void render(int i, int j, float f) {
        this.rootWidget.render(i, j, f);
    }

    @Override
    public void onMouseEvent() {
        this.rootWidget.onMouseEvent();
    }
}
