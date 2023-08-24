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
            protected boolean mouseClicked(int x, int y) {
                self.mouseClicked(x, y);
                return false;
            }

            @Override
            protected int getTotalRenderHeight() {
                return self.getTotalRenderHeight();
            }

            @Override
            protected void renderBackground() {
                self.renderBackground();
            }

            @Override
            protected void renderEntry(int entryIndex, double entryX, double entryY, int entryHeight, Tessellator ts) {
                int x = (int) Math.floor(entryX);
                int y = (int) Math.floor(entryY);
                if (self.renderSelections && self.isWorldSelected(entryIndex)) {
                    GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
                    GL11.glDisable(GL11.GL_TEXTURE_2D);
                    ts.start();
                    self.rootWidget.renderContentSelection(
                        x - 2, y, 220, entryHeight, 1, 0x808080, 0, ts);
                    ts.tessellate();
                    GL11.glEnable(GL11.GL_TEXTURE_2D);
                }
                self.renderStatEntry(entryIndex, x, y, entryHeight, ts);
            }

            @Override
            protected void beforeRender(double x, double y, Tessellator ts) {
                if (self.doRenderStatItemSlot) {
                    int sX = (int) Math.floor(x);
                    int sY = (int) Math.floor(y);
                    self.renderStatItemSlot(sX, sY, ts);
                }
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

    @Overwrite
    public void method_1261(boolean bl, int i) {
        this.doRenderStatItemSlot = bl;
        int contentYOffset = bl ? i : 0;
        this.rootWidget.setContentTopPadding(contentYOffset);
    }

    @Overwrite
    public int method_1262(int i, int j) {
        return this.rootWidget.getEntryUnderPoint(i, j);
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
