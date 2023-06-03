package dev.adventurecraft.awakening.mixin.client.resource;

import dev.adventurecraft.awakening.ACMod;
import net.minecraft.client.resource.TexturePack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

@Mixin(TexturePack.class)
public abstract class MixinTexturePack {

    @Inject(method = "getResourceAsStream", at = @At("HEAD"), cancellable = true)
    private void useAcResourceFirst(String key, CallbackInfoReturnable<InputStream> cir)
        throws URISyntaxException, IOException {

        String acName = "/assets/adventurecraft" + key;
        URL resource = ACMod.class.getResource(acName);
        if (resource == null) {
            // Fallback to base method.
            return;
        }

        File file = new File(resource.toURI());
        if (file.isDirectory()) {
            cir.setReturnValue(null);
        } else {
            InputStream stream = resource.openStream();
            cir.setReturnValue(stream);
        }
    }
}
