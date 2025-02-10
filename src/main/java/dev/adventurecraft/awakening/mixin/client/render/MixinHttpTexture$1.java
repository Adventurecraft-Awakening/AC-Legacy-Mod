package dev.adventurecraft.awakening.mixin.client.render;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.util.UrlUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.charset.StandardCharsets;

@Mixin(targets = "net/minecraft/class_130$1")
public abstract class MixinHttpTexture$1 {

    @Shadow
    private String field_496;

    @Inject(method = "run", at = @At("HEAD"), remap = false, cancellable = true)
    private void skipDownload(CallbackInfo ci) {
        ci.cancel();

        var url = UrlUtil.encodePath(this.field_496, StandardCharsets.UTF_8);
        ACMod.LOGGER.warn("Skipping download of texture \"{}\"", url);
    }
}
