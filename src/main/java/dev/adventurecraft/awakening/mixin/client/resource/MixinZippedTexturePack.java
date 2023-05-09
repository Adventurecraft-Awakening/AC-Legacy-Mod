package dev.adventurecraft.awakening.mixin.client.resource;

import net.minecraft.client.resource.TexturePack;
import net.minecraft.client.resource.ZippedTexturePack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.InputStream;

@Mixin(ZippedTexturePack.class)
public abstract class MixinZippedTexturePack extends TexturePack {

    @Redirect(method = "getResourceAsStream", at = @At(
        value = "INVOKE",
        target = "Ljava/lang/Class;getResourceAsStream(Ljava/lang/String;)Ljava/io/InputStream;",
        remap = false))
    private InputStream useGetResourceAsStreamFromSuper(Class<?> instance, String key) {
        return super.getResourceAsStream(key);
    }
}
