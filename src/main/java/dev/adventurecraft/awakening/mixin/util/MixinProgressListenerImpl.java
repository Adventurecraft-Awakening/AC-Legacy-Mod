package dev.adventurecraft.awakening.mixin.util;

import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.extension.util.ExProgressListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ProgressRenderer;
import net.minecraft.client.ScreenSizeCalculator;
import net.minecraft.client.StopGameException;
import net.minecraft.client.renderer.Tesselator;
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

@Mixin(ProgressRenderer.class)
public abstract class MixinProgressListenerImpl implements ExProgressListener {

    @Shadow
    private long lastTime;

    @Shadow
    private Minecraft minecraft;

    @Shadow
    private boolean noAbort;

    @Unique
    private double progressPercentage;

    @Shadow
    private String title;

    @Shadow
    private String status;

    @ModifyArg(
        method = "progressStart",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/ProgressRenderer;_progressStart(Ljava/lang/String;)V"))
    private String useArgForMessage(String string, @Local(argsOnly = true) String message) {
        return message;
    }

    @Overwrite
    public void _progressStart(String string) {
        this.throwIfNotRunning();
        this.title = string;
        this.status = null;
        this.progressPercentage = -1;
        this.progressStagePercentage(-1);
    }

    @Inject(
        method = "progressStage",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/ProgressRenderer;progressStagePercentage(I)V",
            shift = At.Shift.BEFORE))
    private void resetProgressOnNotify(String par1, CallbackInfo ci) {
        this.progressPercentage = -1;
    }

    @Redirect(
        method = "progressStage",
        at = @At(
            value = "FIELD",
            opcode = Opcodes.PUTFIELD,
            target = "Lnet/minecraft/client/ProgressRenderer;lastTime:J",
            ordinal = 1))
    private void preventSwapDelayWrite(ProgressRenderer instance, long value) {
    }

    @Overwrite
    public void progressStagePercentage(int percentage) {
        this.throwIfNotRunning();
        if (percentage != -1) {
            this.progressPercentage = percentage / 100.0;
        }
        if (System.currentTimeMillis() - this.lastTime < 17L) {
            return;
        }

        var screenScaler = new ScreenSizeCalculator(this.minecraft.options, this.minecraft.width, this.minecraft.height);
        int screenWidth = screenScaler.getWidth();
        int screenHeight = screenScaler.getHeight();
        GL11.glClear(256);
        GL11.glMatrixMode(5889);
        GL11.glLoadIdentity();
        GL11.glOrtho(0.0, screenScaler.guiScaledWidth, screenScaler.guiScaledHeight, 0.0, 100.0, 300.0);
        GL11.glMatrixMode(5888);
        GL11.glLoadIdentity();
        GL11.glTranslatef(0.0f, 0.0f, -200.0f);
        GL11.glClear(16640);
        Tesselator tessellator = Tesselator.instance;
        int n3 = this.minecraft.textures.loadTexture("/gui/background.png");
        GL11.glBindTexture(3553, n3);
        float f = 32.0f;
        tessellator.begin();
        tessellator.color(0x404040);
        tessellator.vertexUV(0.0, screenHeight, 0.0, 0.0, (float) screenHeight / f);
        tessellator.vertexUV(screenWidth, screenHeight, 0.0, (float) screenWidth / f, (float) screenHeight / f);
        tessellator.vertexUV(screenWidth, 0.0, 0.0, (float) screenWidth / f, 0.0);
        tessellator.vertexUV(0.0, 0.0, 0.0, 0.0, 0.0);
        tessellator.end();
        if (this.progressPercentage >= 0) {
            int barWidth = 200;
            int barHeight = 3;
            int barX = screenWidth / 2 - barWidth / 2;
            int barY = screenHeight / 2 + 16;
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            tessellator.begin();

            tessellator.color(0x606060);
            tessellator.vertex(barX, barY, 0.0);
            tessellator.vertex(barX, barY + barHeight, 0.0);
            tessellator.vertex(barX + barWidth, barY + barHeight, 0.0);
            tessellator.vertex(barX + barWidth, barY, 0.0);

            double cursorX = this.progressPercentage * barWidth;
            tessellator.color(0x60FF60);
            tessellator.vertex(barX, barY, 0.0);
            tessellator.vertex(barX, barY + barHeight, 0.0);
            tessellator.vertex(barX + cursorX, barY + barHeight, 0.0);
            tessellator.vertex(barX + cursorX, barY, 0.0);

            tessellator.color(0, 2000, 255);
            tessellator.vertex(barX + cursorX - 1, barY - 1, 0.0);
            tessellator.vertex(barX + cursorX - 1, barY + barHeight + 1, 0.0);
            tessellator.vertex(barX + cursorX, barY + barHeight + 1, 0.0);
            tessellator.vertex(barX + cursorX, barY - 1, 0.0);

            tessellator.end();
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }
        this.minecraft.font.drawShadow(this.title, (screenWidth - this.minecraft.font.width(this.title)) / 2, screenHeight / 2 - 4 - 16, 0xFFFFFF);
        this.minecraft.font.drawShadow(this.status, (screenWidth - this.minecraft.font.width(this.status)) / 2, screenHeight / 2 - 4 + 8, 0xFFFFFF);

        try {
            Display.update();
        } catch (LWJGLException ignored) {
        }
        this.lastTime = System.currentTimeMillis();
    }

    @Override
    public void notifyProgress(String stage, double percentage, boolean forceDraw) {
        this.throwIfNotRunning();
        if (forceDraw) {
            this.lastTime = 0L;
        }
        this.status = stage;
        this.progressPercentage = percentage;
        this.progressStagePercentage(-1);
    }

    private void throwIfNotRunning() {
        if (!this.minecraft.running) {
            if (this.noAbort) {
                return;
            }
            throw new StopGameException();
        }
    }
}
