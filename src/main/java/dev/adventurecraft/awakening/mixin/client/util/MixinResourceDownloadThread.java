package dev.adventurecraft.awakening.mixin.client.util;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.extension.client.util.ExResourceDownloadThread;
import net.minecraft.client.BackgroundDownloader;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

@Mixin(BackgroundDownloader.class)
public abstract class MixinResourceDownloadThread implements ExResourceDownloadThread {

    @Shadow
    private Minecraft minecraft;

    @Shadow
    public abstract void forceReload();

    @WrapWithCondition(
        method = "loadAll",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Minecraft;fileDownloaded(Ljava/lang/String;Ljava/io/File;)V"))
    private boolean onlyLoadOggs(Minecraft instance, String id, File file) {
        String fileName = file.getName();
        return fileName.toLowerCase().endsWith(".ogg");
    }

    @Overwrite
    public void run() {
        forceReload();
    }

    @Inject(
        method = "forceReload",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/BackgroundDownloader;loadAll(Ljava/io/File;Ljava/lang/String;)V",
            shift = At.Shift.AFTER))
    private void loadAtStartup(CallbackInfo ci) {
        ExResourceDownloadThread.loadSoundsFromResources(this.minecraft, ACMod.class, ACMod.getResourceName(""));
    }
}
