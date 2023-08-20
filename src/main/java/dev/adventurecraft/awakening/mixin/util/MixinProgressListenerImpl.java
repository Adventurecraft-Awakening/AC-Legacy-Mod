package dev.adventurecraft.awakening.mixin.util;

import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.extension.util.ExProgressListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.util.ScreenScaler;
import net.minecraft.util.ProgressListenerError;
import net.minecraft.util.ProgressListenerImpl;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ProgressListenerImpl.class)
public abstract class MixinProgressListenerImpl implements ExProgressListener {

    @Shadow
    private long field_1717;

    @Shadow
    private Minecraft client;

    @Shadow
    private boolean ignoreGameRunning;

    @Unique
    private double progressPercentage;

    @Shadow
    private String message;

    @Shadow
    private String field_1714;

    @ModifyArg(
        method = "notifyIgnoreGameRunning",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/ProgressListenerImpl;notify(Ljava/lang/String;)V"))
    private String useArgForMessage(String string, @Local(argsOnly = true) String message) {
        return message;
    }

    @Overwrite
    public void notify(String string) {
        this.throwIfNotRunning();
        this.message = string;
        this.field_1714 = null;
        this.progressPercentage = -1;
        this.progressStagePercentage(-1);
    }

    @Inject(
        method = "notifyProgress",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/ProgressListenerImpl;progressStagePercentage(I)V",
            shift = At.Shift.BEFORE))
    private void resetProgressOnNotify(String par1, CallbackInfo ci) {
        this.progressPercentage = -1;
    }

    @Redirect(
        method = "notifyProgress",
        at = @At(
            value = "FIELD",
            opcode = Opcodes.PUTFIELD,
            target = "Lnet/minecraft/util/ProgressListenerImpl;field_1717:J",
            ordinal = 1))
    private void preventSwapDelayWrite(ProgressListenerImpl instance, long value) {
    }

    @Overwrite
    public void progressStagePercentage(int percentage) {
        this.throwIfNotRunning();
        if (percentage != -1) {
            this.progressPercentage = percentage / 100.0;
        }
        if (System.currentTimeMillis() - this.field_1717 < 17L) {
            return;
        }

        var screenScaler = new ScreenScaler(this.client.options, this.client.actualWidth, this.client.actualHeight);
        int screenWidth = screenScaler.getScaledWidth();
        int screenHeight = screenScaler.getScaledHeight();
        GL11.glClear(256);
        GL11.glMatrixMode(5889);
        GL11.glLoadIdentity();
        GL11.glOrtho(0.0, screenScaler.scaledWidth, screenScaler.scaledHeight, 0.0, 100.0, 300.0);
        GL11.glMatrixMode(5888);
        GL11.glLoadIdentity();
        GL11.glTranslatef(0.0f, 0.0f, -200.0f);
        GL11.glClear(16640);
        Tessellator tessellator = Tessellator.INSTANCE;
        int n3 = this.client.textureManager.getTextureId("/gui/background.png");
        GL11.glBindTexture(3553, n3);
        float f = 32.0f;
        tessellator.start();
        tessellator.color(0x404040);
        tessellator.vertex(0.0, screenHeight, 0.0, 0.0, (float) screenHeight / f);
        tessellator.vertex(screenWidth, screenHeight, 0.0, (float) screenWidth / f, (float) screenHeight / f);
        tessellator.vertex(screenWidth, 0.0, 0.0, (float) screenWidth / f, 0.0);
        tessellator.vertex(0.0, 0.0, 0.0, 0.0, 0.0);
        tessellator.tessellate();
        if (this.progressPercentage >= 0) {
            int barWidth = 200;
            int barHeight = 3;
            int barX = screenWidth / 2 - barWidth / 2;
            int barY = screenHeight / 2 + 16;
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            tessellator.start();

            tessellator.color(0x606060);
            tessellator.addVertex(barX, barY, 0.0);
            tessellator.addVertex(barX, barY + barHeight, 0.0);
            tessellator.addVertex(barX + barWidth, barY + barHeight, 0.0);
            tessellator.addVertex(barX + barWidth, barY, 0.0);

            double cursorX = this.progressPercentage * barWidth;
            tessellator.color(0x60FF60);
            tessellator.addVertex(barX, barY, 0.0);
            tessellator.addVertex(barX, barY + barHeight, 0.0);
            tessellator.addVertex(barX + cursorX, barY + barHeight, 0.0);
            tessellator.addVertex(barX + cursorX, barY, 0.0);

            tessellator.color(0, 2000, 255);
            tessellator.addVertex(barX + cursorX - 1, barY - 1, 0.0);
            tessellator.addVertex(barX + cursorX - 1, barY + barHeight + 1, 0.0);
            tessellator.addVertex(barX + cursorX, barY + barHeight + 1, 0.0);
            tessellator.addVertex(barX + cursorX, barY - 1, 0.0);

            tessellator.tessellate();
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }
        this.client.textRenderer.drawTextWithShadow(this.message, (screenWidth - this.client.textRenderer.getTextWidth(this.message)) / 2, screenHeight / 2 - 4 - 16, 0xFFFFFF);
        this.client.textRenderer.drawTextWithShadow(this.field_1714, (screenWidth - this.client.textRenderer.getTextWidth(this.field_1714)) / 2, screenHeight / 2 - 4 + 8, 0xFFFFFF);

        try {
            Display.update();
        } catch (LWJGLException ignored) {
        }
        this.field_1717 = System.currentTimeMillis();
    }

    @Override
    public void notifyProgress(String stage, double percentage, boolean forceDraw) {
        this.throwIfNotRunning();
        if (forceDraw) {
            this.field_1717 = 0L;
        }
        this.field_1714 = stage;
        this.progressPercentage = percentage;
        this.progressStagePercentage(-1);
    }

    private void throwIfNotRunning() {
        if (!this.client.running) {
            if (this.ignoreGameRunning) {
                return;
            }
            throw new ProgressListenerError();
        }
    }
}
