package dev.adventurecraft.awakening.mixin.client.gui.widget;

import dev.adventurecraft.awakening.common.ScrollableWidget;
import dev.adventurecraft.awakening.extension.client.gui.widget.ExScrollableBaseWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ScrollableBaseWidget;
import net.minecraft.client.render.Tessellator;
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
            protected void mouseClicked(int x, int y) {
                self.mouseClicked(x, y);
            }

            @Override
            protected boolean isEntrySelected(int entryIndex) {
                return self.isWorldSelected(entryIndex);
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
            protected void renderEntry(int entryIndex, double entryX, double entryY, int entryHeight, Tessellator tessellator) {
                int x = (int) Math.floor(entryX);
                int y = (int) Math.floor(entryY);
                self.renderStatEntry(entryIndex, x, y, entryHeight, tessellator);
            }

            @Override
            protected void beforeRender(double x, double y, Tessellator tessellator) {
                if (self.doRenderStatItemSlot) {
                    int sX = (int) Math.floor(x);
                    int sY = (int) Math.floor(y);
                    self.renderStatItemSlot(sX, sY, tessellator);
                }
            }

            @Override
            protected void afterRender(int mouseX, int mouseY, float tickTime, Tessellator tessellator) {
                self.method_1255(mouseX, mouseY);
            }
        };
    }

    @Shadow
    protected abstract int getSize();

    @Shadow
    protected abstract void entryClicked(int var1, boolean var2);

    @Shadow
    protected abstract boolean isWorldSelected(int var1);

    @Shadow
    protected abstract int getTotalRenderHeight();

    @Shadow
    protected abstract void mouseClicked(int x, int y);

    @Shadow
    protected abstract void renderBackground();

    @Shadow
    protected abstract void renderStatEntry(int var1, int var2, int var3, int var4, Tessellator var5);

    @Shadow
    protected abstract void renderStatItemSlot(int i, int j, Tessellator arg);

    @Overwrite
    public void method_1260(boolean bl) {
        this.rootWidget.setRenderSelections(bl);
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
