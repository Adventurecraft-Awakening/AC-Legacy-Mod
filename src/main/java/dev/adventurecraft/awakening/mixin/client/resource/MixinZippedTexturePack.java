package dev.adventurecraft.awakening.mixin.client.resource;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.InputStream;
import net.minecraft.client.skins.FileTexturePack;
import net.minecraft.client.skins.TexturePack;

@Mixin(FileTexturePack.class)
public abstract class MixinZippedTexturePack extends TexturePack {

    @Redirect(method = "getResourceAsStream", at = @At(
        value = "INVOKE",
        target = "Ljava/lang/Class;getResourceAsStream(Ljava/lang/String;)Ljava/io/InputStream;",
        remap = false))
    private InputStream useGetResourceAsStreamFromSuper(Class<?> instance, String key) {
        return super.getResource(key);
    }
}
