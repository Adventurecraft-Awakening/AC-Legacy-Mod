package dev.adventurecraft.awakening.mixin.util;

import dev.adventurecraft.awakening.client.render.RenderSystem;
import dev.adventurecraft.awakening.client.render.RenderScene;
import dev.adventurecraft.awakening.client.render.RenderPhase;
import dev.adventurecraft.awakening.client.render.compat.LegacyPassBridge;
import dev.adventurecraft.awakening.client.render.state.ClearRequest;
import dev.adventurecraft.awakening.client.render.state.Color4;
import dev.adventurecraft.awakening.image.Rgba;
import dev.adventurecraft.awakening.layout.Rect;
import dev.adventurecraft.awakening.layout.IntCorner;
import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.extension.util.ExProgressListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ProgressRenderer;
import net.minecraft.client.ScreenSizeCalculator;
import net.minecraft.client.StopGameException;
import org.lwjgl.LWJGLException;
import dev.adventurecraft.awakening.client.platform.window.GameWindow;
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

        // Loading bypasses Minecraft's main-loop resize hook. Use current logical
        // dimensions for GUI scale and framebuffer pixels for the rendering viewport.
        var logicalSize = GameWindow.logicalSize();
        var screenScaler = new ScreenSizeCalculator(this.minecraft.options,
            Math.max(1, logicalSize.width()), Math.max(1, logicalSize.height()));
        int screenWidth = screenScaler.getWidth();
        int screenHeight = screenScaler.getHeight();
        RenderSystem.ensureFrame();
        RenderSystem.phase(RenderPhase.UI);
        RenderSystem.clear(ClearRequest.all(Color4.BLACK, 1));
        RenderScene.ui(screenScaler.guiScaledWidth, screenScaler.guiScaledHeight);
        try (var ui = LegacyPassBridge.uiContext(this.minecraft.font, this.minecraft.textures)) {
            ui.image(ui.texture("/gui/background.png"), new Rect(0, 0, screenWidth, screenHeight),
                new Rect(0, 0, screenWidth / 32.0, screenHeight / 32.0), new IntCorner(0xff404040));
            if (this.progressPercentage >= 0) {
                int barWidth = 200, barHeight = 3;
                int barX = screenWidth / 2 - barWidth / 2, barY = screenHeight / 2 + 16;
                double cursor = Math.clamp(this.progressPercentage, 0, 1) * barWidth;
                ui.fill(new Rect(barX, barY, barWidth, barHeight), 0xff606060);
                ui.fill(new Rect(barX, barY, cursor, barHeight), 0xff60ff60);
                ui.fill(new Rect(barX + cursor - 1, barY - 1, 1, barHeight + 2), Rgba.fromRgb8(0, 255, 255));
            }
            if (this.title != null) ui.text(this.title, (screenWidth - this.minecraft.font.width(this.title)) / 2F,
                screenHeight / 2F - 20, Rgba.WHITE, true);
            if (this.status != null) ui.text(this.status, (screenWidth - this.minecraft.font.width(this.status)) / 2F,
                screenHeight / 2F + 4, Rgba.WHITE, true);
        }

        try {
            GameWindow.update();
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
