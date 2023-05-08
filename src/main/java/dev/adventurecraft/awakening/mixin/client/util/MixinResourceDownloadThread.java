package dev.adventurecraft.awakening.mixin.client.util;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ResourceDownloadThread;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.io.File;

@Mixin(ResourceDownloadThread.class)
public abstract class MixinResourceDownloadThread {

    @WrapWithCondition(
        method = "method_108",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Minecraft;loadSoundFromDir(Ljava/lang/String;Ljava/io/File;)V"))
    private boolean onlyLoadOggs(Minecraft instance, String id, File file) {
        String fileName = file.getName();
        return fileName.toLowerCase().endsWith(".ogg");
    }
}
