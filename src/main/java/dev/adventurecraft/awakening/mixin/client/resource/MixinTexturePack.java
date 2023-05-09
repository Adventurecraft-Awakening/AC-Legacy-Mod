package dev.adventurecraft.awakening.mixin.client.resource;

import dev.adventurecraft.awakening.ACMod;
import net.minecraft.client.resource.TexturePack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.InputStream;

@Mixin(TexturePack.class)
public abstract class MixinTexturePack {

    @Inject(method = "getResourceAsStream", at = @At("HEAD"), cancellable = true)
    private void useAcResourceFirst(String key, CallbackInfoReturnable<InputStream> cir) {
        String acName = "/assets/adventurecraft" + key;
        InputStream stream = ACMod.class.getResourceAsStream(acName);
        if (stream != null) {
            cir.setReturnValue(stream);
        }
    }
}
