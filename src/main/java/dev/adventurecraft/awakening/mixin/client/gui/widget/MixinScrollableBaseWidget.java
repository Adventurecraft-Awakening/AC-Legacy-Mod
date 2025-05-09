package dev.adventurecraft.awakening.mixin.client.gui.widget;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.common.ScrollableWidget;
import dev.adventurecraft.awakening.extension.client.gui.widget.ExScrollableBaseWidget;
import dev.adventurecraft.awakening.layout.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.Tesselator;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(AbstractSelectionList.class)
public abstract class MixinScrollableBaseWidget implements ExScrollableBaseWidget {

    @Unique
    private ScrollableWidget rootWidget;
    @Unique
    private int hoveredEntry = -1;

    @Shadow
    private boolean renderSelection;
    @Shadow
    private boolean renderHeader;
    @Shadow
    private int headerHeight;

    @Shadow
    protected abstract void renderDecorations(int i, int j);

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(
        Minecraft instance,
        int width,
        int height,
        int contentTop,
        int contentBot,
        int entryHeight,
        CallbackInfo ci
    ) {
        MixinScrollableBaseWidget self = this;
        this.rootWidget = new ScrollableWidget(instance, new IntRect(0, 0, width, height), entryHeight) {
            @Override
            protected int getEntryCount() {
                return self.getItemCount();
            }

            @Override
            protected void entryClicked(int entryIndex, int buttonIndex, boolean doubleClick) {
                if (buttonIndex == 0) {
                    self.method_1267(entryIndex, doubleClick);
                }
            }

            @Override
            protected boolean mouseClicked(IntPoint mouseLocation) {
                Point contentLocation = this.getLocationRelativeToContent(mouseLocation.asFloat());
                if (contentLocation.y <= 0) {
                    self.method_1254((int) contentLocation.x, (int) contentLocation.y);
                }
                return false;
            }

            @Override
            protected int getTotalContentHeight() {
                return self.getMaxPosition() + 8;
            }

            @Override
            protected void renderContentBackground(Tesselator ts, Rect rect, Point scroll) {
                self.renderBackground();
                super.renderContentBackground(ts, rect, scroll);
            }

            @Override
            protected void renderEntry(Tesselator ts, int entryIndex, Point entryLocation, int entryHeight) {
                Point location = entryLocation.floor();
                if (self.renderSelection) {
                    this.renderSelection(ts, entryIndex, entryLocation);
                }
                self.renderEntry(entryIndex, (int) location.x, (int) location.y, entryHeight - 4, ts);
            }

            private void renderSelection(Tesselator ts, int entryIndex, Point point) {
                boolean selected = self.isSelectedItem(entryIndex);
                if (!selected && hoveredEntry != entryIndex) {
                    return;
                }
                var rect = new Rect(point.x - 2, point.y - 2, 220, this.entryHeight);
                var border = new Border(1);

                boolean isHover = !selected && self.hoveredEntry == entryIndex;
                var borderColor = new IntCorner(isHover ? 0x80808080 : 0xff808080);
                var backColor = new IntCorner(isHover ? 0x80000000 : 0xff000000);

                GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glDisable(GL11.GL_CULL_FACE);
                ts.begin();
                self.rootWidget.renderContentSelection(ts, rect, border, borderColor, backColor, null, null);
                ts.end();
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glDisable(GL11.GL_BLEND);
            }

            @Override
            protected void beforeEntryRender(Tesselator ts, IntPoint mouseLocation, Point entryLocation) {
                if (self.renderHeader) {
                    Point headerLocation = entryLocation.floor();
                    int sX = (int) headerLocation.x;
                    int sY = (int) headerLocation.y - this.entryHeight;
                    self.renderHeader(sX, sY, ts);
                }

                self.hoveredEntry = this.getEntryUnderPoint(mouseLocation.asFloat());
            }

            @Override
            protected void afterRender(Tesselator ts, IntPoint mouseLocation, float tickTime) {
                self.renderDecorations(mouseLocation.x, mouseLocation.y);
            }
        };
        this.rootWidget.setLayoutBorder(new IntBorder(0, 0, contentTop, height - contentBot));
        this.updateLayoutPadding();
    }

    @Unique
    private void updateLayoutPadding() {
        IntRect layoutRect = this.rootWidget.getLayoutRect();
        int size = layoutRect.width() / 2 - 110;
        this.rootWidget.setLayoutPadding(new IntBorder(size, size, this.headerHeight + 4, 4));
    }

    @Shadow
    protected abstract int getItemCount();

    @Shadow
    protected abstract void method_1267(int entryIndex, boolean doubleClick);

    @Shadow
    protected abstract boolean isSelectedItem(int entryIndex);

    @Shadow
    protected abstract int getMaxPosition();

    @Shadow
    protected abstract void method_1254(int x, int y);

    @Shadow
    protected abstract void renderBackground();

    @Shadow
    protected abstract void renderEntry(int entryIndex, int x, int y, int height, Tesselator ts);

    @Shadow
    protected abstract void renderHeader(int x, int y, Tesselator ts);

    @Inject(method = "setRenderHeader", at = @At("TAIL"))
    public void setPaddingForRenderHeader(boolean renderHeader, int headerHeight, CallbackInfo ci) {
        this.updateLayoutPadding();
    }

    @Overwrite
    public int getEntryAtPosition(int x, int y) {
        return this.rootWidget.getEntryUnderPoint(new Point(x, y));
    }

    @Overwrite
    public void updateSize(List buttons, int upId, int downId) {
        this.rootWidget.registerButtons(buttons, upId, downId);
    }

    @Overwrite
    public void buttonClicked(Button arg) {
        this.rootWidget.buttonClicked(arg);
    }

    @Overwrite
    public void render(int i, int j, float f) {
        this.rootWidget.render(new IntPoint(i, j), f);
    }

    @Override
    public void onMouseEvent() {
        this.rootWidget.onMouseEvent();
    }
}
